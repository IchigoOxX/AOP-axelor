package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.db.repo.ReceptionReproProductionRepository;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReceptionReproProductionServiceImpl implements ReceptionReproProductionService {
    @Inject
    private ReceptionReproProductionRepository receptReproRepository;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional

    public ReceptionReproProduction Validate(ReceptionReproProduction receptionReproProduction) throws AxelorException {
        ReceptionReproProduction receptProduction = receptReproRepository.find(receptionReproProduction.getId());

        if (receptProduction == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        receptProduction.setReceivedNumber(receptProduction.getReceivedNumber());
        BreedingShed breedingShed = receptProduction.getElevTransfer().getBreedingShed();

        if (breedingShed != null) {
            breedingShed.setFillingDate(LocalDateTime.now());
            if (receptProduction.getBreed() != null) {
                Integer reformDays = receptProduction.getBreed().getReformWeeks();

                if(reformDays != 0 ) {
                    breedingShed.setExpectedReformDate(LocalDateTime.now().plusWeeks(reformDays));

                    if (Objects.equals(receptProduction.getGender(), "M")) {
                        breedingShed.setMalesNumber(breedingShed.getMalesNumber() - receptProduction.getToTransferChickensNumber() + receptProduction.getReceivedNumber());
                    }
                    if (Objects.equals(receptProduction.getGender(), "F")) {
                        breedingShed.setFemalesNumber(breedingShed.getFemalesNumber() - receptProduction.getToTransferChickensNumber() + receptProduction.getReceivedNumber());
                    }

                    if (breedingShed.getStatus() == BreedingShedRepository.STATUS_ACTIVATED) {
                        breedingShed.setStatus(BreedingShedRepository.STATUS_BUSY);
                    }
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("The reform weeks is not specified in breed: " + receptProduction.getBreed().getName() ));
                }

            }
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
        }
        receptProduction.getCenter().setCurrentDailyAge(receptProduction.getDailyAge());
        receptProduction.getCenter().setCurrentWeeklyAge(receptProduction.getWeeklyAge());
        receptProduction.getCenter().setCurrentTrackingNumber(receptProduction.getTrackingNumber());
        receptProduction.getCenter().setCurrentBreed(receptProduction.getBreed());

        if (receptProduction.getToTransferChickensNumber() - receptProduction.getReceivedNumber() > 0) {
            StockMove dispatcherMove = CreateOrUpdateStockMove(receptProduction);
            for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
                BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(dispatcherMove.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                    if (!line.getProduct().getAllowNegativeStock()) {
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + dispatcherMove.getFromStockLocation().getName() + " !")
                            );
                        }
                    }
            }
            dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_REPRO_PRODUCTION_RECEPTION);
            receptProduction.setDispatchStock(dispatcherMove);
            stockMoveService.plan(dispatcherMove);
            stockMoveService.realize(dispatcherMove, true);
            dispatcherMove.setRealDate(LocalDate.now());
        }

        receptProduction.setStatus(ReceptionReproProductionRepository.STATUS_RECEIVED);

        StockMove stockMove = receptProduction.getElevTransfer().getDispatchStock();
        if (stockMove != null) {
            stockMove.setReceptionReproProduction(receptProduction);
            stockMoveService.realize(stockMove, true);
            stockMove.setSubType(StockMoveRepository.SUB_TYPE_REPRO_PRODUCTION_RECEPTION);
            stockMove.setRealDate(LocalDate.now());
        } else {
            stockMove = null;
        }
        receptProduction.getElevTransfer().setStatus(ElevTransferRepository.STATUS_RECEIVED);
        return receptReproRepository.save(receptProduction);

    }
    @Transactional
    public StockMove CreateOrUpdateStockMove(ReceptionReproProduction receptionReproProduction)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (receptionReproProduction == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        ReceptionReproProduction recept = receptReproRepository.find(receptionReproProduction.getId());

        if (recept == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();

        StockLocation virtualVariousOutingsLocation = app.getVirtualVariousOutingsLocation();

        StockMove stockMove;

        if (recept.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if (recept.getCenter().getUnit().getCompany() != null) {
                values.put("company", recept.getCenter().getUnit().getCompany());
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if (recept.getBreedingShed().getStockLocation() != null) {
                values.put("fromStockLocation", recept.getBreedingShed().getStockLocation());
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed : " + recept.getBreedingShed().getName() + " !"));
            }
            if (virtualVariousOutingsLocation != null) {
                values.put("toStockLocation", virtualVariousOutingsLocation);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }
            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(recept.getCenter());
            stockMove.setReceptionReproProduction(recept);

        } else {
            stockMove = recept.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (recept.getToTransferChickensNumber() - recept.getReceivedNumber() > 0) {

            StockMoveLine sm = new StockMoveLine();
            if(Objects.equals(recept.getGender(), "M")) {
                if (starterMaleChick != null) {
                    sm.setProduct(starterMaleChick);
                } else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_CHICK_NOT_SPECIFIED));
                }
            }
            else if(Objects.equals(recept.getGender(), "F")) {
                if (starterFemaleChick != null) {
                    sm.setProduct(starterFemaleChick);
                } else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_CHICK_NOT_SPECIFIED));
                }
            }
            sm.setQty(BigDecimal.valueOf(recept.getToTransferChickensNumber() - recept.getReceivedNumber()));
            sm.setRealQty(BigDecimal.valueOf(recept.getToTransferChickensNumber() - recept.getReceivedNumber()));

        if (recept.getBreedingShed().getStockLocation() != null) {
            sm.setFromStockLocation(recept.getBreedingShed().getStockLocation());
        } else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No stock location was specified for breeding shed :" + recept.getBreedingShed().getName() + " !"));

        }
        if (recept.getBreedingShed().getStockLocation() != null) {
            sm.setToStockLocation(virtualVariousOutingsLocation);
        } else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
        }
        sm.setProductName(sm.getProduct().getName());
        sm.setUnit(sm.getProduct().getUnit());
        sm.setTrackingNumber(recept.getTrackingNumber());
        stockMove.addStockMoveLineListItem(sm);
    }

        return stockMoveRepo.save(stockMove);
    }


    @Transactional
    public ReceptionReproProduction Cancel(ReceptionReproProduction receptionReproProduction) throws AxelorException {
        ReceptionReproProduction receptProduction = receptReproRepository.find(receptionReproProduction.getId());

        if (receptProduction == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        receptProduction.getElevTransfer().setStatus(ElevTransferRepository.STATUS_EXECUTING);

        BreedingShed breedingShed = receptProduction.getElevTransfer().getBreedingShed();
        if (breedingShed != null) {
            if(Objects.equals(receptProduction.getGender(), "M")) {
                breedingShed.setMalesNumber(breedingShed.getMalesNumber() + receptProduction.getToTransferChickensNumber() - receptProduction.getReceivedNumber());
            }
            if(Objects.equals(receptProduction.getGender(), "F")) {
                breedingShed.setFemalesNumber(breedingShed.getFemalesNumber() + receptProduction.getToTransferChickensNumber() - receptProduction.getReceivedNumber());
            }
            breedingShed.setStatus(BreedingShedRepository.STATUS_ACTIVATED);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
        }

        StockMove stockMove = receptProduction.getElevTransfer().getDispatchStock();

        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
            stockMoveService.goBackToDraft(stockMove);
            stockMoveService.plan(stockMove);
        } else {
            stockMove = null;
        }

        receptProduction.setStatus(ReceptionReproProductionRepository.STATUS_CANCELED);
        return receptReproRepository.save(receptProduction);

    }
}




