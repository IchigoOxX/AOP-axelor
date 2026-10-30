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
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IncubatorExitServiceImpl implements IncubatorExitService{
    @Inject
    private IncubatorExitRepository incubatorExitRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private HatcherEntryRepository hatcherEntryRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public IncubatorExit Validate(IncubatorExit incubatorExit) throws AxelorException {
        IncubatorExit incubatorExit1 = incubatorExitRepository.find(incubatorExit.getId());

        if (incubatorExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND));
        }


        if(incubatorExit1.getHatchery()!= null){
            if(incubatorExit1.getHatchery().getUnit() != null) {
                incubatorExit1.setUnit(incubatorExit1.getHatchery().getUnit());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No unit was specified for hatchery: "+ incubatorExit1.getHatchery().getName()));
            }
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
        }


        if (!incubatorExit1.getIncubatorExitLines().isEmpty()) {
            AtomicInteger lostEggs = new AtomicInteger();
            incubatorExit1.getIncubatorExitLines().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_STOPPED);
                        line.setStatus(IncubatorTrolleyRepository.STATUS_STOPPED);
                        lostEggs.addAndGet(line.getLostEggs());
                    }
                });

            incubatorExit1.setSumLostHatchingEggs(lostEggs.get());
        }

        //incubatorExit1.getIncubator().setStopDate(LocalDateTime.now());
        StockMove dispatcherMove = CreateOrUpdateStockMove(incubatorExit1);
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
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_INCUBATOR_EXITS);
        incubatorExit1.setDispatchStock(dispatcherMove);
        stockMoveService.plan(dispatcherMove);
        incubatorExit1.setStatus(IncubatorExitRepository.STATUS_EXECUTING);
        incubatorExit1.getIncubatorEntry().setStatus(IncubatorEntryRepository.STATUS_EXECUTED);

        HatcherEntry hatcherEntry = new HatcherEntry();
        hatcherEntry.setDateTimeIncubatorExit(incubatorExit1.getDatetime());
        hatcherEntry.setIncubatorEntry(incubatorExit1.getIncubatorEntry());
        hatcherEntry.setDateTimeIncubatorEntry(incubatorExit1.getDateTimeIncubatorEntry());
        hatcherEntry.setDateTimeIncubatorExitEntry(incubatorExit1.getDateTimeIncubatorExit());
        hatcherEntry.setIncubator(incubatorExit1.getIncubator());
        hatcherEntry.setBreed(incubatorExit1.getBreed());
        hatcherEntry.setHatchery(incubatorExit1.getHatchery());
        hatcherEntry.setDispatchStock(incubatorExit1.getDispatchStock());
        hatcherEntry.setDateTime(LocalDateTime.now());
        hatcherEntry.setIncubatorExit(incubatorExit1);
        hatcherEntry.setStatus(HatcherEntryRepository.STATUS_NOT_EXECUTED);

        for (IncubatorExitLine x : incubatorExit1.getIncubatorExitLines()) {
            HatcherEntryLine rline = new HatcherEntryLine();

            rline.setIncubatorTrolley(x.getIncubatorTrolley());
            rline.setBreedingShed(x.getBreedingShed());
            rline.setCenter(x.getCenter());
            rline.setTrackingNumber(x.getTrackingNumber());
            rline.setCurrentDailyAge(x.getCurrentDailyAge());
            rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
            rline.setLoadingType(x.getLoadingType());
            rline.setNumberOfOac(x.getEggsNumber() - x.getLostEggs());
            rline.setNumberOfOacBasket((x.getEggsNumber() - x.getLostEggs()) / hatcherEntry.getHatchery().getCapacityOfAnEggBasket());
            hatcherEntry.addHatcherEntryLinesListItem(rline);
        }
        hatcherEntryRepository.save(hatcherEntry);


        return incubatorExitRepository.save(incubatorExit1);
    }

    @Transactional
    public IncubatorExit Cancel(IncubatorExit incubatorExit) throws AxelorException {
        IncubatorExit incubatorExit1 = incubatorExitRepository.find(incubatorExit.getId());

        if (incubatorExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND));
        }
        incubatorExit1.setReasonOfCancel(incubatorExit.getReasonOfCancel());
        if (!incubatorExit1.getIncubatorExitLines().isEmpty()) {
            incubatorExit1.getIncubatorExitLines().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                        line.setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                    }
                });
        }

        StockMove stockMove = incubatorExit1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }

        incubatorExit1.setStatus(IncubatorExitRepository.STATUS_CANCELED);
        return incubatorExitRepository.save(incubatorExit1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(IncubatorExit incubatorExit)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (incubatorExit == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND));
        }

        IncubatorExit incubatorExit1 = incubatorExitRepository.find(incubatorExit.getId());

        if (incubatorExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_SPECIFIED));
        }

        Product OACEggs = app.getHatchingEggProduct();
        StockLocation incubatorDefaultLocation = app.getHatcheryDefaultIncubatorLocation();
        StockLocation hatcherDefaultLocation = app.getHatcherDefaultLocation();

        StockMove stockMove;

        if (incubatorExit1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(incubatorExit1.getHatchery().getUnit().getCompany() != null) {
                values.put("company", incubatorExit1.getHatchery().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(incubatorDefaultLocation != null){
                values.put("fromStockLocation", incubatorDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if(hatcherDefaultLocation != null ){
                values.put("toStockLocation", hatcherDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
        }
        else {
            stockMove = incubatorExit1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }

        stockMove.setIncubatorExit(incubatorExit1);

        if (!incubatorExit1.getIncubatorExitLines().isEmpty()) {
            for (IncubatorExitLine line : incubatorExit1.getIncubatorExitLines()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getEggsNumber() != 0) {
                        if (OACEggs != null) {
                            sm.setProduct(OACEggs);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.OAC_EGGS_NOT_SPECIFIED));
                        }

                        sm.setQty(BigDecimal.valueOf(line.getEggsNumber() - line.getLostEggs()));
                        sm.setRealQty(BigDecimal.valueOf(line.getEggsNumber() - line.getLostEggs()));
                        if(incubatorDefaultLocation != null) {
                            sm.setFromStockLocation(incubatorDefaultLocation);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_DEFAULT_LOCATION_NOT_SPECIFIED));
                        }
                        if(hatcherDefaultLocation != null) {
                            sm.setToStockLocation(hatcherDefaultLocation);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_DEFAULT_LOCATION_NOT_SPECIFIED));
                        }

                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                        stockMove.addStockMoveLineListItem(sm);
                    }
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public IncubatorExit Revalidate(IncubatorExit incubatorExit) throws AxelorException {
        IncubatorExit incubatorExit1 = incubatorExitRepository.find(incubatorExit.getId());

        if (incubatorExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND));
        }
        if (!incubatorExit1.getIncubatorExitLines().isEmpty()) {
            incubatorExit1.getIncubatorExitLines().forEach(
                    line -> {
                        if (line != null) {
                                line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                                line.setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                        }
                    });
        }
        StockMove stockMove = incubatorExit1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.goBackToDraft(stockMove);
        }
        incubatorExit1.setStatus(IncubatorExitRepository.STATUS_NOT_EXECUTED);
        return incubatorExitRepository.save(incubatorExit1);
    }
}




