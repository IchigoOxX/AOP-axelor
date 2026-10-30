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
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.StartElevMovement;
import com.gl.apps.pf.db.StartElevMovementLine;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.db.repo.StartElevMovementRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StartElevMovementServiceImpl implements StartElevMovementService{
    @Inject
    private StartElevMovementRepository startElevMovementRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public StartElevMovement Validate(StartElevMovement startElevMovement) throws AxelorException {
        StartElevMovement elevMov = startElevMovementRepository.find(startElevMovement.getId());
        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_MOVEMENT_NOT_FOUND));
        }
        StockMove stockMove;
        if(!elevMov.getStartElevMovementLineList().isEmpty()){
            elevMov.getStartElevMovementLineList().forEach(
                line -> {
                    line.getBreedingShed().setDisplacementDate(elevMov.getDateTime());
                    BreedingShed b = line.getBreedingShed();
                    if (b.getReceivedFemalesNumber() > 0) {
                        b.setFemalesNumber(b.getFemalesNumber() + line.getMovedNumber());
                    }
                    else if (b.getReceivedMalesNumber() > 0) {
                        b.setMalesNumber(b.getMalesNumber() + line.getMovedNumber());
                    }

                }
            );
        }
        int sumOfMovNumber = elevMov.getStartElevMovementLineList().stream()
                .mapToInt(line -> line.getMovedNumber())
                .sum();

        BreedingShed b1 = elevMov.getBreedingShed();
        if(b1.getReceivedFemalesNumber() > 0) {
            b1.setFemalesNumber(elevMov.getTotalNumbers() - sumOfMovNumber);
        }
        else if (b1.getReceivedMalesNumber() > 0) {
            b1.setMalesNumber(elevMov.getTotalNumbers() - sumOfMovNumber);
        }

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = CreateOrUpdateStockMove(elevMov);
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
        elevMov.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
        dispatcherMove.setCurrentBreed(elevMov.getBreed());
        dispatcherMove.setCurrentTrackingNumber(elevMov.getTrackingNumber());
        dispatcherMove.setCurrentDailyAge(elevMov.getDailyAge());
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);
        dispatcherMove.setRealDate(LocalDate.now());

        elevMov.setStatus(StartElevMovementRepository.STATUS_EXECUTING);
        return startElevMovementRepository.save(elevMov);

    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(StartElevMovement startElevMovement)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (startElevMovement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_MOVEMENT_NOT_FOUND));
        }

        StartElevMovement elevMov = startElevMovementRepository.find(startElevMovement.getId());

        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_MOVEMENT_NOT_SPECIFIED));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();
        StockLocation virtualStartupLocation = app.getVirtualStartupLocation();

        StockMove stockMove;

        if (elevMov.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            values.put("company", elevMov.getCenter().getUnit().getCompany());
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(virtualStartupLocation != null){
                values.put("fromStockLocation", virtualStartupLocation);
                values.put("toStockLocation", virtualStartupLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(elevMov.getCenter());
            stockMove.setStartElevMovement(elevMov);
        }
        else {
            stockMove = elevMov.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!elevMov.getStartElevMovementLineList().isEmpty() ) {
            for (StartElevMovementLine line : elevMov.getStartElevMovementLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getBreedingShed().getFemalesNumber() != 0) {
                        if (starterFemaleChick != null) {
                            sm.setProduct(starterFemaleChick);
                        } else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_CHICK_NOT_SPECIFIED));
                        }
                        sm.setQty(BigDecimal.valueOf(line.getMovedNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getMovedNumber()));
                    } else if (line.getBreedingShed().getMalesNumber() != 0) {
                        if (starterMaleChick != null) {
                            sm.setProduct(starterMaleChick);
                        } else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_CHICK_NOT_SPECIFIED));
                        }
                        sm.setQty(BigDecimal.valueOf(line.getMovedNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getMovedNumber()));
                    }
                    if (elevMov.getBreedingShed().getStockLocation() != null) {
                        sm.setFromStockLocation(elevMov.getBreedingShed().getStockLocation());
                    } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed : " + elevMov.getBreedingShed().getName() + " !"));
                    }
                    if (line.getBreedingShed().getStockLocation() != null) {
                        sm.setToStockLocation(line.getBreedingShed().getStockLocation());
                    } else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_MISSING_FIELD,
                                I18n.get("No stock location was specified for breeding shed : " + line.getBreedingShed().getStockLocation().getName() + " !"));
                    }
                    sm.setProductName(sm.getProduct().getName());
                    sm.setUnit(sm.getProduct().getUnit());
                    sm.setTrackingNumber(elevMov.getTrackingNumber());
                    stockMove.addStockMoveLineListItem(sm);
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public StartElevMovement Cancel(StartElevMovement startElevMovement) throws AxelorException {
        StartElevMovement elevMov = startElevMovementRepository.find(startElevMovement.getId());
        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_MOVEMENT_NOT_FOUND));
        }
        StockMove stockMove;
        if(!elevMov.getStartElevMovementLineList().isEmpty()){
            elevMov.getStartElevMovementLineList().forEach(
                    line -> {
                        line.getBreedingShed().setDisplacementDate(elevMov.getDateTime());
                        BreedingShed b = line.getBreedingShed();
                        if (b.getReceivedFemalesNumber() > 0) {
                            b.setFemalesNumber(b.getFemalesNumber() - line.getMovedNumber());
                        }
                        else if (b.getReceivedMalesNumber() > 0) {
                            b.setMalesNumber(b.getMalesNumber() - line.getMovedNumber());
                        }

                    }
            );
        }
        int sumOfMovNumber = elevMov.getStartElevMovementLineList().stream()
                .mapToInt(line -> line.getMovedNumber())
                .sum();

        BreedingShed b1 = elevMov.getBreedingShed();
        if(b1.getReceivedFemalesNumber() > 0) {
            b1.setFemalesNumber(b1.getFemalesNumber() + sumOfMovNumber);
        }
        else if (b1.getReceivedMalesNumber() > 0) {
            b1.setMalesNumber(b1.getMalesNumber() + sumOfMovNumber);
        }

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = elevMov.getDispatchStock();
        if(dispatcherMove != null ) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        elevMov.setStatus(StartElevMovementRepository.STATUS_CANCELED);
        return startElevMovementRepository.save(elevMov);

    }

}




