package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class IncubatorEntryServiceImpl implements IncubatorEntryService{
    @Inject
    private IncubatorEntryRepository incubatorEntryRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private IncubatorExitRepository incubatorExitRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public IncubatorEntry Validate(IncubatorEntry incubatorEntry) throws AxelorException {
        IncubatorEntry incubatorEntry1 = incubatorEntryRepository.find(incubatorEntry.getId());
        if (incubatorEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }
        if(incubatorEntry1.getHatchery()!= null){
            if(incubatorEntry1.getHatchery().getUnit() != null) {
                incubatorEntry1.setUnit(incubatorEntry1.getHatchery().getUnit());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No unit was specified for hatchery: "+ incubatorEntry1.getHatchery().getName()));
            }
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
        }

        if (!incubatorEntry1.getIncubatorEntryLines().isEmpty() ) {
            incubatorEntry1.getIncubatorEntryLines().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                        line.setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                        line.getIncubatorTrolley().setIncubator(incubatorEntry1.getIncubator());
                    }
                });
        }
        if(incubatorEntry1.getLoadingProgramLine() != null){
            incubatorEntry1.getLoadingProgramLine().setStatus(IncubatorsLoadingProgramLineRepository.STATUS_EXECUTING);
        }

        // Create new incubator exit
        IncubatorExit incubatorExit = new IncubatorExit();
        incubatorExit.setIncubatorEntry(incubatorEntry1);
        incubatorExit.setHatchery(incubatorEntry1.getHatchery());
        incubatorExit.setBreed(incubatorEntry1.getBreed());
        incubatorExit.setDateTimeIncubatorEntry(incubatorEntry1.getDatetime());
        incubatorExit.setIncubator(incubatorEntry1.getIncubator());
        incubatorExit.setDatetime(LocalDateTime.now());
        incubatorExit.setStatus(IncubatorExitRepository.STATUS_NOT_EXECUTED);

        for (IncubatorEntryLine x : incubatorEntry1.getIncubatorEntryLines()) {
                IncubatorExitLine rline = new IncubatorExitLine();
                rline.setIncubatorTrolley(x.getIncubatorTrolley());
                rline.setBreedingShed(x.getBreedingShed());
                rline.setCenter(x.getCenter());
                rline.setStatus(x.getStatus());
                rline.setTrackingNumber(x.getTrackingNumber());
                rline.setLoadingType(x.getLoadingType());
                rline.setCurrentDailyAge(x.getCurrentDailyAge());
                rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
                rline.setTraysNumber(x.getTraysNumber());
                rline.setEggsNumber(x.getEggsNumber());
                incubatorExit.addIncubatorExitLine(rline);
            incubatorExit.setDateTimeIncubatorExit(incubatorExit.getDateTimeIncubatorEntry().plusDays(incubatorExit.getIncubatorEntry().getHatchery().getNumberOfIncubationDays()).plusHours(incubatorExit.getIncubatorEntry().getHatchery().getNumberOfIncubationHours()));
        }

        incubatorExitRepository.save(incubatorExit);

        StockMove dispatcherMove = CreateOrUpdateStockMove(incubatorEntry1);
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
        incubatorEntry1.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_INCUBATOR_ENTRIES);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove, true);
        dispatcherMove.setRealDate(LocalDate.now());
        incubatorEntry1.setStatus(IncubatorEntryRepository.STATUS_EXECUTING);

        return incubatorEntryRepository.save(incubatorEntry1);
    }

    @Transactional
    public IncubatorEntry Cancel(IncubatorEntry incubatorEntry) throws AxelorException {
        IncubatorEntry incubatorEntry1 = incubatorEntryRepository.find(incubatorEntry.getId());
        if (incubatorEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }
        incubatorEntry1.setReasonOfCancel(incubatorEntry.getReasonOfCancel());
        if (!incubatorEntry1.getIncubatorEntryLines().isEmpty() ) {
            incubatorEntry1.getIncubatorEntryLines().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_BUSY);
                        line.setStatus(IncubatorTrolleyRepository.STATUS_BUSY);
                    }
                });
        }

        StockMove stockMove = incubatorEntry1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }
        incubatorEntry1.setStatus(IncubatorEntryRepository.STATUS_CANCELED);

        return incubatorEntryRepository.save(incubatorEntry1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(IncubatorEntry incubatorEntry)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (incubatorEntry == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }

        IncubatorEntry incubatorEntry1 = incubatorEntryRepository.find(incubatorEntry.getId());

        if (incubatorEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }

        Product OACEggs = app.getHatchingEggProduct();
        StockLocation hatcheryEggDefaultLocation = app.getHatchryEggDefaultLocation();
        StockLocation incubatorDefaultLocation = app.getHatcheryDefaultIncubatorLocation();

        StockMove stockMove;

        if (incubatorEntry1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(incubatorEntry1.getHatchery().getUnit().getCompany() != null) {
                values.put("company", incubatorEntry1.getHatchery().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if(hatcheryEggDefaultLocation != null ){
                values.put("fromStockLocation", hatcheryEggDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if(incubatorDefaultLocation != null ){
                values.put("toStockLocation", incubatorDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
        }
        else {
            stockMove = incubatorEntry1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        stockMove.setIncubatorEntry(incubatorEntry1);

        if (!incubatorEntry1.getIncubatorEntryLines().isEmpty()) {
            for (IncubatorEntryLine line : incubatorEntry1.getIncubatorEntryLines()) {
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
                        sm.setQty(BigDecimal.valueOf(line.getEggsNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getEggsNumber()));
                        sm.setFromStockLocation(hatcheryEggDefaultLocation);
                        sm.setToStockLocation(incubatorDefaultLocation);
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
    public IncubatorEntry Revalidate(IncubatorEntry incubatorEntry) throws AxelorException {
        IncubatorEntry incubatorEntry1 = incubatorEntryRepository.find(incubatorEntry.getId());
        if (incubatorEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }
        if (!incubatorEntry1.getIncubatorEntryLines().isEmpty()) {
            incubatorEntry1.getIncubatorEntryLines().forEach(
                line -> {
                    if (line != null) {
                        if (line.getIncubatorTrolley().getStatus() != IncubatorTrolleyRepository.STATUS_RESERVED ) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_RESERVED);
                        line.setStatus(IncubatorTrolleyRepository.STATUS_RESERVED);
                        }
                    else try {
                            throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_TROLLEY_IS_RESERVED));
                        } catch (AxelorException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        }
        incubatorEntry1.setStatus(IncubatorEntryRepository.STATUS_NOT_EXECUTED);
        StockMove stockMove = incubatorEntry1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.goBackToDraft(stockMove);
        }
        return incubatorEntryRepository.save(incubatorEntry1);
    }

}




