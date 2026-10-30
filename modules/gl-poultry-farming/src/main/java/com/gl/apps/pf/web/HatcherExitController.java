package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.FleshLoadingProgramLineRepository;
import com.gl.apps.pf.db.repo.HatcherEntryRepository;
import com.gl.apps.pf.db.repo.HatcherExitRepository;
import com.gl.apps.pf.db.repo.IncubatorExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcherExitService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HatcherExitController {
    @Inject private HatcherExitService hatcherExitService;
    @Inject private FleshLoadingProgramLineRepository fleshLoadingProgramLineRepository;
    @Inject private SaleOrderLineRepository saleOrderLineRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;

    public void getLinesFromHatcherEntry(ActionRequest request, ActionResponse response)
            throws AxelorException {

        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);

        if (hatcherExit != null && hatcherExit.getHatcherEntry() != null) {
            hatcherExit.clearHatcherExitLinesList();

            for (HatcherEntryLine x : hatcherExit.getHatcherEntry().getHatcherEntryLinesList()) {
                if (x.getHatcherEntry() == hatcherExit.getHatcherEntry() && x.getHatcherEntry().getStatus() == HatcherEntryRepository.STATUS_EXECUTING) {
                    HatcherExitLine rline = new HatcherExitLine();
                    rline.setIncubatorTrolley(x.getIncubatorTrolley());
                    rline.setHatcherTrolley(x.getHatcherTrolley());
                    rline.setBreedingShed(x.getBreedingShed());
                    rline.setCenter(x.getCenter());
                    rline.setFirstLoadingNumber(x.getNumberOfOac());
                    rline.setStatus(x.getStatus());
                    rline.setCurrentDailyAge(x.getCurrentDailyAge());
                    rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
                    rline.setTrackingNumber(x.getTrackingNumber());
                    rline.setLoadingType(x.getLoadingType());
                    rline.setHatcher(x.getHatcher());
                    hatcherExit.setCapacity(x.getNumberOfOac());
                    hatcherExit.addHatcherExitLinesListItem(rline);
                }
            }
            hatcherExit.setDateTimeHatcherEntryExit(hatcherExit.getDateTimeIncubatorExit().plusDays(hatcherExit.getHatcherEntry().getHatchery().getNumberOfHatchingDays()).plusHours(hatcherExit.getHatcherEntry().getHatchery().getNumberOfHoursOfHatching()));
        }

        response.setValues(hatcherExit);
    }

    @Transactional
    public void getFleshLoadingProgramLines(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);

        if (hatcherExit != null) {
            hatcherExit.clearFleshLoadingProgramList();

            List<FleshLoadingProgramLine> fleshLoadingProgramLines = fleshLoadingProgramLineRepository
                    .all()
                    .filter("self.status = ?1 and self.fleshLoadingProgram.isValid = true", FleshLoadingProgramLineRepository.STATUS_NOT_EXECUTED)
                    .fetch();

            hatcherExit.setFleshLoadingProgramList(fleshLoadingProgramLines);

//            for (FleshLoadingProgramLine line : fleshLoadingProgramLineRepository.all().fetch()) {
//                if (line != null) {
//                    if(line.getStatus() == FleshLoadingProgramLineRepository.STATUS_NOT_EXECUTED){
//                        FleshLoadingProgramLine rline = new FleshLoadingProgramLine();
//
//                        rline.setCenter(line.getFleshLoadingProgram().getCenter());
//                        rline.setFleshLoadingProgram(line.getFleshLoadingProgram());
//                        rline.setLineNumber(line.getLineNumber());
//                        rline.setBreed(line.getBreed());
//                        rline.setBreedingShed(line.getBreedingShed());
//                        rline.setPlannedLoadingDatetime(line.getPlannedLoadingDatetime());
//                        rline.setNumberOfChicksPerCage(line.getNumberOfChicksPerCage());
//                        rline.setNumberOfChicks(line.getNumberOfChicks());
//                        rline.setAge(line.getAge());
//                        rline.setPeriod(line.getPeriod());
//                        rline.setSlaughterFromDatetime(line.getSlaughterFromDatetime());
//                        rline.setSlaughterToDatetime(line.getSlaughterToDatetime());
//
//                        hatcherExit.addFleshLoadingProgramListItem(rline);
                  //  }

               // }
          //  }
        }
        response.setValues(hatcherExit);
    }

    @Transactional
    public void HatcherExitValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);

        if (hatcherExit != null ) {
            hatcherExitService.Validate(hatcherExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_SPECIFIED));
        }
        response.setValues(hatcherExit);
        response.setReload(true);
    }

    @Transactional
    public void HatcherExitCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherExit hatcherExit =
                Beans.get(HatcherExitRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (hatcherExit != null) {
            hatcherExit.setReasonOfCancel(cancelReason);
            hatcherExitService.Cancel(hatcherExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcherExit);
        response.setReload(true);
    }
    @Transactional
    public void HatcherExitRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);

        if (hatcherExit != null ) {
            hatcherExitService.Revalidate(hatcherExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcherExit);
        response.setReload(true);
    }

    @Transactional
    public void HatcherExitCalculateReceptionValues(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);

        if (hatcherExit != null ) {
            hatcherExitService.CalculateReceptionValues(hatcherExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcherExit);
        response.setReload(true);
    }
    public void getLinesSaleOrderLines(ActionRequest request, ActionResponse response)
            throws AxelorException {
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        Product  chairChicks = app.getChairChicks();
        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);
        LocalDateTime dateHatcherExit = hatcherExit.getDateTimeHatcherEntryExit();

        List<SaleOrderLine> saleOrderLines = saleOrderLineRepository
                .all()
                .filter("self.saleOrder.statusSelect = ?1 and self.saleOrder.creationDate = ?2 and self.product = ?3",
                        SaleOrderRepository.STATUS_ORDER_CONFIRMED, dateHatcherExit, chairChicks)
                .fetch();

        Double sumQty = saleOrderLines.stream().mapToDouble(out -> Double.parseDouble(out.getQty().toString())).sum();
        hatcherExit.setExternNumber(sumQty.intValue());
        hatcherExit.setCustomerSaleOrderLine(saleOrderLines);


        response.setValues(hatcherExit);
    }

    public void divideTotalEntryOnLines(ActionRequest request, ActionResponse response)
            throws AxelorException {

        HatcherExit hatcherExit = request.getContext().asType(HatcherExit.class);


        if (hatcherExit != null && hatcherExit.getHatcherExitLinesList() != null) {
            List<HatcherExitLine> lines = hatcherExit.getHatcherExitLinesList();
            int numberOfLines = lines.size();

            if (numberOfLines > 0) {
                // Distribute totalSafe
                int totalSafe = hatcherExit.getSafe();
                int baseSafe = totalSafe / numberOfLines;
                int remainderSafe = totalSafe % numberOfLines;

                // Distribute totalMortality
                int totalMortality = hatcherExit.getMortality();
                int baseMortality = totalMortality / numberOfLines;
                int remainderMortality = totalMortality % numberOfLines;

                // Distribute totalSort
                int totalSort = hatcherExit.getSort();
                int baseSort = totalSort / numberOfLines;
                int remainderSort = totalSort % numberOfLines;

                for (int i = 0; i < numberOfLines; i++) {
                    HatcherExitLine line = lines.get(i);

                    // Set safe
                    if (i == numberOfLines - 1) {
                        line.setSafe(baseSafe + remainderSafe);
                        BigDecimal safeRate = BigDecimal.valueOf((baseSafe + remainderSafe) * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setSafeRate(safeRate);
                    } else {
                        BigDecimal safeRate = BigDecimal.valueOf(baseSafe * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setSafe(baseSafe);
                        line.setSafeRate(safeRate);
                    }

                    // Set mortality
                    if (i == numberOfLines - 1) {
                        line.setMortality(baseMortality + remainderMortality);
                        BigDecimal mortalityRate = BigDecimal.valueOf((baseMortality + remainderMortality) * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setMortalityRate(mortalityRate);
                    } else {
                        line.setMortality(baseMortality);
                        BigDecimal mortalityRate = BigDecimal.valueOf(baseMortality * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setMortalityRate(mortalityRate);
                    }

                    // Set sort
                    if (i == numberOfLines - 1) {
                        line.setSort(baseSort + remainderSort);
                        BigDecimal sortRate = BigDecimal.valueOf((baseSort + remainderSort) * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setSortRate(sortRate);
                    } else {
                        line.setSort(baseSort);
                        BigDecimal sortRate = BigDecimal.valueOf(baseSort * 100)
                                .divide(BigDecimal.valueOf(hatcherExit.getTotalEntry()), 2, RoundingMode.HALF_UP);
                        line.setSortRate(sortRate);
                    }
                    line.setTotal(line.getSafe()+ line.getSort() + line.getMortality());
                    line.setTotalRate(line.getSafeRate().add(line.getSortRate()).add(line.getMortalityRate()));
                }
            }
            hatcherExit.setHatcherExitLinesList(lines);
            response.setValue("hatcherExitLinesList", lines);

        }
    }
    public void redistributeSafeValues(ActionRequest request, ActionResponse response) throws AxelorException {
        HatcherExitLine changedLine = request.getContext().asType(HatcherExitLine.class);
        if (changedLine != null ) {
            hatcherExitService.redistributeSafeValues(changedLine);
        }
        response.setValues(changedLine);
        response.setReload(true);

    }
    public void redistributeSafeValuesMortality(ActionRequest request, ActionResponse response) throws AxelorException {
        HatcherExitLine changedLine = request.getContext().asType(HatcherExitLine.class);
        if (changedLine != null ) {
            hatcherExitService.redistributeSafeValuesMortality(changedLine);
        }
        response.setValues(changedLine);
        response.setReload(true);

    }
    public void redistributeSafeValuesSort(ActionRequest request, ActionResponse response) throws AxelorException {
        HatcherExitLine changedLine = request.getContext().asType(HatcherExitLine.class);
        if (changedLine != null ) {
            hatcherExitService.redistributeSafeValuesSort(changedLine);
        }
        response.setValues(changedLine);
        response.setReload(true);
    }

//    public void test(ActionRequest request, ActionResponse response)
//            throws AxelorException {
//
//        HatcherExitLine hatcherExitLine = request.getContext().asType(HatcherExitLine.class);
//        //redistributeSafeValues(hatcherExitLine);
//        Map<String, Object>  _parent = (Map<String, Object>) request.getContext().get("_parent");
//        HatcherExit hatcherExit = new HatcherExit();
//
//        hatcherExit.setDateTimeHatcherEntryExit((LocalDateTime) _parent.get("dateTimeHatcherEntryExit"));
//        hatcherExit.setIncubatorExit((IncubatorExit) _parent.get("incubatorExit"));
//        hatcherExit.setDateTimeIncubatorExit((LocalDateTime) _parent.get("dateTimeIncubatorExit"));
//        hatcherExit.setExternNumber((Integer) _parent.get("externNumber"));
//        hatcherExit.setTotalEntry((Integer) _parent.get("totalEntry"));
//        hatcherExit.setCapacity((Integer) _parent.get("capacity"));
//        hatcherExit.setReference((String) _parent.get("reference"));
//        hatcherExit.setDatetime((LocalDateTime) _parent.get("datetime"));
//        hatcherExit.setSortRate((BigDecimal) _parent.get("sortRate"));
//        hatcherExit.setDispatchStock((StockMove) _parent.get("dispatchStock"));
//        hatcherExit.setIncubator((Incubator) _parent.get("incubator"));
//        hatcherExit.setHatcherExitLinesList((List<HatcherExitLine>) _parent.get("hatcherExitLinesList"));
//        hatcherExit.setDateTimeHatcherEntry((LocalDateTime) _parent.get("dateTimeHatcherEntry"));
//        hatcherExit.setNumberOfChicksPerPackage((Integer) _parent.get("numberOfChicksPerPackage"));
//        hatcherExit.setDeliveryCapacity((Integer) _parent.get("deliveryCapacity"));
//        hatcherExit.setHatchery((Hatchery) _parent.get("hatchery"));
//        hatcherExit.setInternNumber((Integer) _parent.get("internNumber"));
//        hatcherExit.setSuccessRate((BigDecimal) _parent.get("successRate"));
//        hatcherExit.setSafeRate((BigDecimal) _parent.get("safeRate"));
//        hatcherExit.setSort((Integer) _parent.get("sort"));
//        hatcherExit.setHatcherEntry((HatcherEntry) _parent.get("hatcherEntry"));
//        hatcherExit.setCustomerSaleOrderLine((List<SaleOrderLine>) _parent.get("customerSaleOrderLine"));
//        hatcherExit.setVersion((Integer) _parent.get("version"));
//        hatcherExit.setTheoryNumber((Integer) _parent.get("theoryNumber"));
//        hatcherExit.setBreed((Breed) _parent.get("breed"));
//        hatcherExit.setLoadingProgramLine((IncubatorsLoadingProgramLine) _parent.get("loadingProgramLine"));
//        hatcherExit.setHatcher((String) _parent.get("hatcher"));
//        hatcherExit.setMortalityRate((BigDecimal) _parent.get("mortalityRate"));
//        hatcherExit.setNumberOfPackage((Integer) _parent.get("numberOfPackage"));
//        hatcherExit.setNumberOfPackagingDelivery((Integer) _parent.get("numberOfPackagingDelivery"));
//        hatcherExit.setFleshLoadingProgramList((List<FleshLoadingProgramLine>) _parent.get("fleshLoadingProgramList"));
//        hatcherExit.setSafe((Integer) _parent.get("safe"));
//        hatcherExit.setMortality((Integer) _parent.get("mortality"));
//        hatcherExit.setStatus((Integer) _parent.get("status"));
//
//        response.setValue("_parent", _parent);
//        response.setValue("hatcherExitLinesList", hatcherExit.getHatcherExitLinesList());
//
//       // response.setValue("hatcherExitLinesList", hatcherExitLine.getHatcherExit().getHatcherExitLinesList());
//    }



}

