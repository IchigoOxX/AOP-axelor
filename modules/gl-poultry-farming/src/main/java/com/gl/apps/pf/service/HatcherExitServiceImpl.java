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
import com.google.common.util.concurrent.AtomicDouble;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HatcherExitServiceImpl implements HatcherExitService {
    @Inject
    private HatcherExitRepository hatcherExitRepository;
    @Inject
    private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private HatcherExitLineRepository hatcherExitLineRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public HatcherExit Validate(HatcherExit hatcherExit) throws AxelorException {

        HatcherExit hatcherExit1 = hatcherExitRepository.find(hatcherExit.getId());
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        int nbrHoursFlesh = app.getFleshHoursNumber();

        if (hatcherExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }

        if(hatcherExit1.getHatchery()!= null){
            if(hatcherExit1.getHatchery().getUnit() != null) {
                hatcherExit1.setUnit(hatcherExit1.getHatchery().getUnit());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No unit was specified for hatchery: "+ hatcherExit1.getHatchery().getName()));
            }
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
        }

        if (!hatcherExit1.getHatcherExitLinesList().isEmpty()) {
            hatcherExit1.getHatcherExitLinesList().forEach(
                line -> {
                    if (line != null) {
                        line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_STOPPED);
                        line.getIncubatorTrolley().setStatus(IncubatorRepository.STATUS_STOPPED);
                        line.setStatus(HatcherTrolleyRepository.STATUS_STOPPED);
                        //TODO: set status of hatcher to STOPPED
                    }
                });
        }

        if (!hatcherExit1.getFleshLoadingProgramList().isEmpty()) {
            for (FleshLoadingProgramLine line1 : hatcherExit1.getFleshLoadingProgramList()) {
                if (line1 != null) {
                    line1.setRealLoadingDatetime(line1.getPlannedLoadingDatetime().plusHours(nbrHoursFlesh));
                    StockMove receptionDispatcherMove = null;
                    receptionDispatcherMove = CreateOrUpdateStockMoveFleshChickRecept(line1);
                    line1.setFleshReceptionDispatchStock(receptionDispatcherMove);

                }
            }
        }
        CalculateReceptionValues(hatcherExit1);

        StockMove dispatcherMove = CreateOrUpdateStockMove(hatcherExit1);
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
        hatcherExit1.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_HATCHER_EXITS);
        stockMoveService.plan(dispatcherMove);

        hatcherExit1.setStatus(HatcherExitRepository.STATUS_EXECUTING);
        hatcherExit1.getHatcherEntry().setStatus(HatcherEntryRepository.STATUS_EXECUTED);

        return hatcherExitRepository.save(hatcherExit1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(HatcherExit hatcherExit)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (hatcherExit == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }

        HatcherExit hatcherExit1 = hatcherExitRepository.find(hatcherExit.getId());

        if (hatcherExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_SPECIFIED));
        }

        Product chairChicks = app.getChairChicks();
        StockLocation HatcheryEggDefaultLocation = app.getHatchryEggDefaultLocation();
        StockLocation hatcherDefaultLocation = app.getHatcherDefaultLocation();

        StockMove stockMove;

        if (hatcherExit1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(hatcherExit1.getHatchery().getUnit().getCompany() != null) {
                values.put("company", hatcherExit1.getHatchery().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(hatcherDefaultLocation != null){
                values.put("fromStockLocation", hatcherDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if(HatcheryEggDefaultLocation != null){
                values.put("toStockLocation", HatcheryEggDefaultLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
        }
        else {
            stockMove = hatcherExit1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }

        stockMove.setHatcherExit(hatcherExit1);

        if (!hatcherExit1.getHatcherExitLinesList().isEmpty()) {
            for (HatcherExitLine line : hatcherExit1.getHatcherExitLinesList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getSafe() != 0) {
                        if (chairChicks != null) {
                            sm.setProduct(chairChicks);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.CHAIR_CHICKS_NOT_SPECIFIED));
                        }
                        sm.setQty(BigDecimal.valueOf(line.getSafe()));
                        sm.setRealQty(BigDecimal.valueOf(line.getSafe()));
                        if(hatcherDefaultLocation != null){
                            sm.setFromStockLocation(hatcherDefaultLocation);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_DEFAULT_LOCATION_NOT_SPECIFIED));
                        }
                        if(HatcheryEggDefaultLocation != null){
                            sm.setToStockLocation(HatcheryEggDefaultLocation);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
                        }

                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                        stockMove.addStockMoveLineListItem(sm);
                    }
                }
            }
        }
        if(hatcherExit1.getNumberOfPackagingDelivery()!= 0) {
            StockMoveLine sm1 = new StockMoveLine();
            if (hatcherExit1.getHatchery().getPackagingProduct() != null) {
                sm1.setProduct(hatcherExit1.getHatchery().getPackagingProduct());

            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_PACKAGING_PRODUCT_NOT_SPECIFIED));
            }
            sm1.setQty(BigDecimal.valueOf(hatcherExit1.getNumberOfPackagingDelivery()));
            sm1.setRealQty(BigDecimal.valueOf(hatcherExit1.getNumberOfPackagingDelivery()));
            if (hatcherDefaultLocation != null) {
                sm1.setFromStockLocation(hatcherDefaultLocation);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if (HatcheryEggDefaultLocation != null) {
                sm1.setToStockLocation(HatcheryEggDefaultLocation);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            sm1.setProductName(hatcherExit1.getHatchery().getPackagingProduct().getName());
            sm1.setUnit(sm1.getProduct().getUnit());
            stockMove.addStockMoveLineListItem(sm1);
        }

        return stockMoveRepo.save(stockMove);
    }


    @Transactional
    public StockMove CreateOrUpdateStockMoveFleshChickRecept(FleshLoadingProgramLine fleshLoadingProgramLine) throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        Product chairChick = app.getChairChicks();
        StockLocation hatcheryEggLocation = app.getHatchryEggDefaultLocation();
        StockLocation chairLocation = app.getChairDefaultLocation();

        StockMove stockMove = null;
        if (fleshLoadingProgramLine.getNumberOfChicks() != 0 ) {
            Map<String, Object> values = new HashMap<>();
            if(fleshLoadingProgramLine.getBreedingShed().getCenter().getUnit().getCompany() != null) {
                values.put("company", fleshLoadingProgramLine.getBreedingShed().getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(hatcheryEggLocation != null){
                values.put("fromStockLocation", hatcheryEggLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if(chairLocation != null){
                values.put("toStockLocation", chairLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);

            StockMoveLine sm = new StockMoveLine();
            if (chairChick != null) {
                sm.setProduct(chairChick);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.CHAIR_CHICKS_NOT_SPECIFIED));
            }
            sm.setQty(BigDecimal.valueOf(fleshLoadingProgramLine.getNumberOfChicks()));
            sm.setRealQty(BigDecimal.valueOf(fleshLoadingProgramLine.getNumberOfChicks()));
            if (hatcheryEggLocation != null ){
                sm.setFromStockLocation(hatcheryEggLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            if(fleshLoadingProgramLine.getBreedingShed().getStockLocation() != null){
                sm.setToStockLocation(fleshLoadingProgramLine.getBreedingShed().getStockLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get("No stock location was specified for breeding shed : " + fleshLoadingProgramLine.getBreedingShed().getName() + " !"));
            }
            sm.setProductName(sm.getProduct().getName());
            sm.setUnit(sm.getProduct().getUnit());

            stockMove.addStockMoveLineListItem(sm);
        }

        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public HatcherExit Cancel(HatcherExit hatcherExit) throws AxelorException {
        HatcherExit hatcherExit1 = hatcherExitRepository.find(hatcherExit.getId());

        if (hatcherExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }
        hatcherExit1.setReasonOfCancel(hatcherExit.getReasonOfCancel());
        if (!hatcherExit1.getHatcherExitLinesList().isEmpty()) {
            hatcherExit1.getHatcherExitLinesList().forEach(
                    line -> {
                        if (line != null) {
                            line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                            line.setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                        }
                    });
        }
        hatcherExit1.setStatus(IncubatorExitRepository.STATUS_CANCELED);
        //incubatorExit1.getIncubatorEntry().setStatus(IncubatorExitRepository.STATUS_CANCELED);

        StockMove stockMove = hatcherExit1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }

        return hatcherExitRepository.save(hatcherExit1);
    }

    @Transactional
    public HatcherExit Revalidate(HatcherExit hatcherExit) throws AxelorException {
        HatcherExit hatcherExit1 = hatcherExitRepository.find(hatcherExit.getId());

        if (hatcherExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }
        if (!hatcherExit1.getHatcherExitLinesList().isEmpty()) {
            hatcherExit1.getHatcherExitLinesList().forEach(
                    line -> {
                        if (line != null) {
                            line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                            line.setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                        }
                    });
        }
        hatcherExit1.setStatus(HatcherExitRepository.STATUS_NOT_EXECUTED);
        StockMove stockMove = hatcherExit1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.goBackToDraft(stockMove);
        }
        return hatcherExitRepository.save(hatcherExit1);
    }

    @Transactional
    public HatcherExit CalculateReceptionValues(HatcherExit hatcherExit) throws AxelorException {
        HatcherExit hatcherExit1 = hatcherExitRepository.find(hatcherExit.getId());

        if (hatcherExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }
        int listSize = hatcherExit1.getHatcherExitLinesList().size();

        if (!hatcherExit1.getHatcherExitLinesList().isEmpty()) {
            AtomicInteger safeNumber = new AtomicInteger();
            AtomicInteger sortNumber = new AtomicInteger();
            AtomicInteger mortalityNumber = new AtomicInteger();
            AtomicDouble safeRate = new AtomicDouble();
            AtomicDouble sortRate = new AtomicDouble();
            AtomicDouble mortalityRate = new AtomicDouble();

            hatcherExit1.getHatcherExitLinesList().forEach(
                line -> {
                    if (line != null) {
                        safeNumber.addAndGet(line.getSafe());
                        sortNumber.addAndGet(line.getSort());
                        mortalityNumber.addAndGet(line.getMortality());
                        safeRate.addAndGet(line.getSafeRate().doubleValue());
                        sortRate.addAndGet(line.getSortRate().doubleValue());
                        mortalityRate.addAndGet(line.getMortalityRate().doubleValue());
                    }
                });

            hatcherExit1.setSafeRate(BigDecimal.valueOf(safeRate.get() /listSize ));
            hatcherExit1.setSortRate(BigDecimal.valueOf(sortRate.get() / listSize));
            hatcherExit1.setMortalityRate(BigDecimal.valueOf(mortalityRate.get() / listSize));
        }

        return hatcherExitRepository.save(hatcherExit1);
    }

    @Transactional
    public HatcherExit redistributeSafeValues(HatcherExitLine changedLine) throws AxelorException {
        if (changedLine == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }

        hatcherExitRepository.save(changedLine.getHatcherExit());

        HatcherExit hatcherExit = changedLine.getHatcherExit();
        if (hatcherExit != null && hatcherExit.getHatcherExitLinesList() != null) {
            List<HatcherExitLine> lines = hatcherExit.getHatcherExitLinesList();
            int numberOfLines = lines.size();

            if (numberOfLines > 1 && lines.contains(changedLine)) {
                int totalSafe = hatcherExit.getSafe();
                int changedSafe = changedLine.getSafe();

                int remainingSafe = totalSafe - changedSafe;

                for (HatcherExitLine line : lines) {
                    if (!line.equals(changedLine)) {
                        line.setSafe(remainingSafe);
                    }
                    if (line.equals(changedLine)) {
                        line.setSafe(changedSafe);
                    }
                }

                for (HatcherExitLine line : lines) {
                    BigDecimal safeRate = BigDecimal.valueOf(line.getSafe() * 100)
                            .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                    line.setSafeRate(safeRate);
                    line.setTotal(line.getSafe()+ line.getSort() + line.getMortality());
                    line.setTotalRate(line.getSafeRate().add(line.getSortRate()).add(line.getMortalityRate()));
                }

            }
            hatcherExit.setHatcherExitLinesList(lines);
        }

        return hatcherExitRepository.save(hatcherExit);
    }

    @Transactional
    public HatcherExit redistributeSafeValuesMortality(HatcherExitLine changedLine) throws AxelorException {
        if (changedLine == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }

        hatcherExitRepository.save(changedLine.getHatcherExit());

        HatcherExit hatcherExit = changedLine.getHatcherExit();
        if (hatcherExit != null && hatcherExit.getHatcherExitLinesList() != null) {
            List<HatcherExitLine> lines = hatcherExit.getHatcherExitLinesList();
            int numberOfLines = lines.size();

            if (numberOfLines > 1 && lines.contains(changedLine)) {

                int totalMortality = hatcherExit.getMortality();
                int changedMortality = changedLine.getMortality();

                int remainingMortality = totalMortality - changedMortality;

                for (HatcherExitLine line : lines) {
                    if (!line.equals(changedLine)) {
                        line.setMortality(remainingMortality);
                    }
                    if (line.equals(changedLine)) {
                        line.setMortality(changedMortality);
                    }
                }

                for (HatcherExitLine line : lines) {
                    BigDecimal mortalityRate = BigDecimal.valueOf(line.getMortality() * 100)
                            .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                    line.setMortalityRate(mortalityRate);
                    line.setTotal(line.getSafe()+ line.getSort() + line.getMortality());
                    line.setTotalRate(line.getSafeRate().add(line.getSortRate()).add(line.getMortalityRate()));
                }
            }
            hatcherExit.setHatcherExitLinesList(lines);
        }

        return hatcherExitRepository.save(hatcherExit);
    }

    @Transactional
    public HatcherExit redistributeSafeValuesSort(HatcherExitLine changedLine) throws AxelorException {
        if (changedLine == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));
        }

        hatcherExitRepository.save(changedLine.getHatcherExit());

        HatcherExit hatcherExit = changedLine.getHatcherExit();
        if (hatcherExit != null && hatcherExit.getHatcherExitLinesList() != null) {
            List<HatcherExitLine> lines = hatcherExit.getHatcherExitLinesList();
            int numberOfLines = lines.size();

            if (numberOfLines > 1 && lines.contains(changedLine)) {
                int totalSort = hatcherExit.getSort();
                int changedSort = changedLine.getSort();

                int remainingSort = totalSort - changedSort;

                for (HatcherExitLine line : lines) {
                    if (!line.equals(changedLine)) {
                        line.setSort(remainingSort);
                    }
                    if (line.equals(changedLine)) {
                        line.setSort(changedSort);
                    }
                }

                for (HatcherExitLine line : lines) {
                    BigDecimal sortRate = BigDecimal.valueOf(line.getSort() * 100)
                            .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                    line.setSortRate(sortRate);
                    line.setTotal(line.getSafe()+ line.getSort() + line.getMortality());
                    line.setTotalRate(line.getSafeRate().add(line.getSortRate()).add(line.getMortalityRate()));
                }
            }
            hatcherExit.setHatcherExitLinesList(lines);
        }

        return hatcherExitRepository.save(hatcherExit);
    }

}




