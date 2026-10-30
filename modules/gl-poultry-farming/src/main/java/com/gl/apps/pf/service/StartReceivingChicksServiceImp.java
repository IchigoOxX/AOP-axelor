package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
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
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartReceivingChicksServiceImp implements StartReceivingChicksService {
    @Inject
    private StartReceivingChicksRepository startReceingChecksRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private CenterRepository centerRepo;
    @Inject private BreedingShedRepository breedingShedRepo;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockLocationUtilsService stockLocationService;

    @Transactional
    public StartReceivingChicks Validate(StartReceivingChicks startReceivingChicks) throws AxelorException {
        StartReceivingChicks receivingChicks = startReceingChecksRepository.find(startReceivingChicks.getId());
        if (receivingChicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_RECEIVING_CHECKS_NOT_FOUND));
        }

        StockMove stockMove;
        Center center =  null;
        BreedingShed breedingShed = null;

        if (receivingChicks.getSupplierReception() != null) {
            stockMove = stockMoveRepo.find(receivingChicks.getSupplierReception().getId());
        } else {
            stockMove = null;
        }
        if (receivingChicks.getCenter() != null) {
            center = centerRepo.find(receivingChicks.getCenter().getId());
        }

        assert center != null;

        center.setCurrentBreed(receivingChicks.getBreed());
        center.setCurrentTrackingNumber(receivingChicks.getTrackingNumber());
        center.setCurrentDailyAge(receivingChicks.getDailyAge());

        if(!receivingChicks.getReceivingChicksLineList().isEmpty()){
            receivingChicks.getReceivingChicksLineList().forEach(
                line -> {
                    if (receivingChicks.getBreed() != null) {
                        Integer transferDays = receivingChicks.getBreed().getTransferWeeks();

                        if (transferDays != 0) {
                            if (line.getNumberOfFemales() != 0 || line.getNumberOfMales() != 0) {
                                line.getBreedingShed().setBreed(receivingChicks.getBreed());
                                line.getBreedingShed().setCurrentBatchNumber(receivingChicks.getTrackingNumber());
                                line.getBreedingShed().setDailyAge(receivingChicks.getDailyAge());
                                line.getBreedingShed().setStatus(BreedingShedRepository.STATUS_BUSY);
                                line.getBreedingShed().setFillingDate(LocalDateTime.now());
                                line.getBreedingShed().setExpectedTransferDate(LocalDateTime.now().plusWeeks(transferDays));
                                line.getBreedingShed().setActivationDate(receivingChicks.getBreedingShedActivationDate());
                                int females = line.getNumberOfFemales() != null ? line.getNumberOfFemales() : 0;
                                int males = line.getNumberOfMales() != null ? line.getNumberOfMales() : 0;
                                line.getBreedingShed().setFemalesNumber(females);
                                line.getBreedingShed().setReceivedFemalesNumber(females);
                                line.getBreedingShed().setMalesNumber(males);
                                line.getBreedingShed().setReceivedMalesNumber(males);
                            }
                        }
                        else {
                            try {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get("The transfer weeks is not specified in breed: " + receivingChicks.getBreed().getName()));
                            } catch (AxelorException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                        
                }
            );
        }

        stockMove.setRealDate(LocalDate.now());

        StockMove dispatcherMove = CreateOrUpdateStockMove(receivingChicks);
        if(AuthUtils.getUser().getActiveCompany()!= null ) {
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
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("You must specify the active company in the user !")
            );
        }

        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_CHICKS_RECEPTION);
        receivingChicks.setDispatchStock(dispatcherMove);
        dispatcherMove.setRealDate(LocalDate.now());
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove, true);

        receivingChicks.setStatus(StartReceivingChicksRepository.STATUS_RECEIVED);
        return startReceingChecksRepository.save(receivingChicks);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(StartReceivingChicks startReceivingChicks)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (startReceivingChicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_RECEIVING_CHECKS_NOT_FOUND));
        }

        StartReceivingChicks receivingChicks = startReceingChecksRepository.find(startReceivingChicks.getId());

        if (receivingChicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIVING_CHICKS_NOT_SPECIFIED));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();

        StockLocation EggDefaultBreedingLocation = app.getEggsDefaultBreedingLocation();
        StockMove stockMove;

        if (startReceivingChicks.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(receivingChicks.getCenter().getUnit().getCompany() != null) {
                values.put("company", receivingChicks.getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
                if (startReceivingChicks.getSupplierReception().getToStockLocation() != null) {
                    values.put("fromStockLocation", startReceivingChicks.getSupplierReception().getToStockLocation());
                }
                else try {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.STOCK_LOCATION_SUPPLIER_RECEPTION_NOT_FOUND));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }
                if ( EggDefaultBreedingLocation != null){
                    values.put("toStockLocation", EggDefaultBreedingLocation);
                }
                else try {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.STOCK_LOCATION_BREEDING_EGG_NOT_FOUND));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(startReceivingChicks.getCenter());
            stockMove.setReceivingChicks(startReceivingChicks);
        }
        else {
            stockMove = startReceivingChicks.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!receivingChicks.getReceivingChicksLineList().isEmpty() ) {
            for (StartReceivingChicksLine line : receivingChicks.getReceivingChicksLineList()) {
                if (line != null) {
                    if (line.getNumberOfFemales() != null && line.getNumberOfFemales() > 0) {
                        StockMoveLine sm = new StockMoveLine();
                        if (starterFemaleChick != null) {
                            sm.setProduct(starterFemaleChick);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_PRODUCT_NOT_DEFINED));
                        }
                        sm.setQty(BigDecimal.valueOf(line.getNumberOfFemales()));
                        sm.setRealQty(BigDecimal.valueOf(line.getNumberOfFemales()));
                        sm.setFromStockLocation(stockMove.getFromStockLocation());
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());

                        if (line.getBreedingShed().getStockLocation() != null) {
                            sm.setToStockLocation(line.getBreedingShed().getStockLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STOCK_LOCATION_BREEDING_SHED_NOT_FOUND));
                        }
                        stockMove.addStockMoveLineListItem(sm);
                    }

                    if (line.getNumberOfMales() != null && line.getNumberOfMales() > 0) {
                        StockMoveLine smMale = new StockMoveLine();
                        if (starterMaleChick != null) {
                            smMale.setProduct(starterMaleChick);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_PRODUCT_NOT_DEFINED));
                        }
                        smMale.setQty(BigDecimal.valueOf(line.getNumberOfMales()));
                        smMale.setRealQty(BigDecimal.valueOf(line.getNumberOfMales()));
                        smMale.setFromStockLocation(stockMove.getFromStockLocation());
                        smMale.setProductName(smMale.getProduct().getName());
                        smMale.setUnit(smMale.getProduct().getUnit());

                        if (line.getBreedingShed().getStockLocation() != null) {
                            smMale.setToStockLocation(line.getBreedingShed().getStockLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STOCK_LOCATION_BREEDING_SHED_NOT_FOUND));
                        }
                        stockMove.addStockMoveLineListItem(smMale);
                    }
                }
            }
        }
    return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public StartReceivingChicks Cancel(StartReceivingChicks startReceivingChicks) throws AxelorException {
        StartReceivingChicks receivingChicks = startReceingChecksRepository.find(startReceivingChicks.getId());
        if (receivingChicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_RECEIVING_CHECKS_NOT_FOUND));
        }

        StockMove stockMove;
        Center center =  null;
        BreedingShed breedingShed = null;

        if (receivingChicks.getSupplierReception() != null) {
            stockMove = stockMoveRepo.find(receivingChicks.getSupplierReception().getId());
        } else {
            stockMove = null;
        }
        if (receivingChicks.getCenter() != null) {
            center = centerRepo.find(receivingChicks.getCenter().getId());
        }

        assert center != null;
        List<StartReceivingChicks> sameCenterReceivingChicks = startReceingChecksRepository.all()
                .filter("self.center = ?1 AND self.status = ?2", center, StartReceivingChicksRepository.STATUS_RECEIVED)
                .fetch();
        int size = sameCenterReceivingChicks.size();

        if (size == 1) {
            center.setCurrentBreed(null);
            center.setCurrentTrackingNumber(null);
            center.setCurrentDailyAge(0);
        }

        if(!receivingChicks.getReceivingChicksLineList().isEmpty()) {
            receivingChicks.getReceivingChicksLineList().forEach(
                line -> {
                    if (line.getNumberOfFemales() != 0 || line.getNumberOfMales() != 0) {
                        line.getBreedingShed().setBreed(null);
                        line.getBreedingShed().setCurrentBatchNumber(null);
                        line.getBreedingShed().setDailyAge(0);
                        line.getBreedingShed().setStatus(BreedingShedRepository.STATUS_ACTIVATED);
                        line.getBreedingShed().setFillingDate(null);
                        line.getBreedingShed().setFemalesNumber(0);
                        line.getBreedingShed().setReceivedFemalesNumber(0);
                        line.getBreedingShed().setMalesNumber(0);
                        line.getBreedingShed().setReceivedMalesNumber(0);
                    }
                }
            );
        }

        stockMoveService.plan(stockMove);

        StockMove dispatcherMove = receivingChicks.getDispatchStock();
        if(dispatcherMove != null) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        receivingChicks.setStatus(StartReceivingChicksRepository.STATUS_CANCELED);
        return startReceingChecksRepository.save(receivingChicks);
    }

//    @Transactional
//    public StartReceivingChicks SetActivationBreedingShedList(StartReceivingChicks startReceivingChicks, Center center) throws AxelorException {
//        StartReceivingChicks receivingChicks = startReceingChecksRepository.find(startReceivingChicks.getId());
//
//        LocalDateTime now = LocalDateTime.now();
//        if (receivingChicks == null) {
//            throw new AxelorException(
//                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                    I18n.get(PoultryFarmingExceptionMessage.START_RECEIVING_CHECKS_NOT_FOUND));
//        }
//        if( center != null ) {
//            for (BreedingShed x : receivingChicks.getCenter().getBreedingShedList()) {
//                if (x.getActivationDate()!= null && (x.getActivationDate().isBefore(now) || x.getActivationDate().isEqual(now))) {
//                    x.setStatus(BreedingShedRepository.STATUS_ACTIVATED);
//                }
////                if (x.getStoppedDate()!= null && (x.getStoppedDate().isBefore(now) || x.getStoppedDate().isEqual(now))) {
////                    x.setStatus(BreedingShedRepository.STATUS_STOPPED);
////                }
////                if (x.getClosedDate()!= null && (x.getClosedDate().isBefore(now) || x.getClosedDate().isEqual(now))) {
////                    x.setStatus(BreedingShedRepository.STATUS_CLOSED);
////                }
//                breedingShedRepo.save(x);
//            }
//        }
//        return startReceingChecksRepository.save(receivingChicks);
//    }
    }




