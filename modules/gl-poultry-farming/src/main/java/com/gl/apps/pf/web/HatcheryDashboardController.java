package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import java.time.ZonedDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HatcheryDashboardController {
    @Inject private IncubatorsLoadingProgramLineRepository incubatorsLoadingProgramLineRepository;
    @Inject private IncubatorEntryRepository incubatorEntryRepository;
    @Inject private IncubatorExitRepository incubatorExitRepository;
    @Inject private HatcherEntryRepository hatcherEntryRepository;
    @Inject private HatcherEntryLineRepository hatcherEntryLineRepository;
    @Inject private HatcherExitLineRepository hatcherExitLineRepository;
    @Inject private HatcherExitRepository hatcherExitRepository;
    @Inject private AppPoultryFarmingRepository appPoultryFarmingRepository;
    @Inject private SaleOrderRepository saleOrderRepository;
    @Inject private FleshChicksReceptionRepository fleshChicksReceptionRepository;
    @Inject private IncubatorEntryLineRepository incubatorEntryLineRepository;
    @Inject private FleshLoadingProgramLineRepository fleshLoadingProgramLineRepository;
    @Inject private HatcheryEggsReceptionLineRepository hatcheryEggsReceptionLineRepository;
    @Inject private HatcheryDeliverySaleRepository hatcheryDeliverySaleRepository;

    public void getLinesHatcheryDashboard(ActionRequest request, ActionResponse response) throws AxelorException {
        AppPoultryFarming app = appPoultryFarmingRepository.all().fetchOne();


        List<HatcheryDashboard> hatcheryDashboardList = new ArrayList<>();
        String startIncubatorDateTime =  request.getContext().get("startIncubationDatetime").toString();
        String endIncubatorDateTime =  request.getContext().get("endIncubationDatetime").toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date endDate = null;
        try {
            endDate = dateFormat.parse(endIncubatorDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date newEndDate = calendar.getTime();

        String formattedDate = dateFormat.format(newEndDate);

        if(request.getContext().get("status") == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No status was selected ! please select at least 1 status"));
        }

        String selectedStatuses =  request.getContext().get("status").toString();

        List<Integer> selectedStatuses1 = Arrays.stream(selectedStatuses.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        if(request.getContext().get("trackingNumber") == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No tracking number was specified !"));
        }

        Map<String, Object> trackingNumberMap = (Map<String, Object>) request.getContext().get("trackingNumber");
        Integer trackingNumberId = (Integer) trackingNumberMap.get("id");

        for (IncubatorsLoadingProgramLine incubatorsLoadingProgramLine : incubatorsLoadingProgramLineRepository.all().fetch()) {
            for (IncubatorEntry incubatorEntry : incubatorEntryRepository.all().filter("self.loadingProgramLine = ? " +
                                    "AND self.loadingProgramLine.incubationDatetime between ? and ? " +
                                    "AND self.loadingProgramLine.status in :statuses",
                            incubatorsLoadingProgramLine.getId(),startIncubatorDateTime, formattedDate)
                    .bind("statuses", selectedStatuses1)
                    .fetch()) {
                for (IncubatorExit incubatorExit : incubatorExitRepository.all().filter("self.incubatorEntry = ?", incubatorEntry.getId()).fetch()) {
                    for (IncubatorEntryLine incubatorEntryLine : incubatorEntryLineRepository.all().filter("self.incubatorEntry = ? AND self.trackingNumber.id = ?", incubatorEntry.getId(), trackingNumberId).fetch()) {
                        for (HatcherEntry hatcherEntry : hatcherEntryRepository.all().filter("self.incubatorExit = ?", incubatorExit.getId()).fetch()) {
                            for (HatcherExit hatcherExit : hatcherExitRepository.all().filter("self.hatcherEntry = ?", hatcherEntry.getId()).fetch()) {
                                for (FleshChicksReception fleshChicksReception : fleshChicksReceptionRepository.all().filter("self.hatcherExit = ?", hatcherExit.getId()).fetch()) {
                                    for (HatcheryDeliverySale hatcheryDeliverySale : hatcheryDeliverySaleRepository.all().filter("self.status = 1 AND self.hatchery = ?",hatcherExit.getHatchery()).fetch()) {
                                        HatcheryDashboard hatcheryDashboard = new HatcheryDashboard();

                                        hatcheryDashboard.setCompleteRefTrackingNumber(fleshChicksReception.getCompleteRefTrackingNumber());
                                        hatcheryDashboard.setProgramLine(incubatorsLoadingProgramLine);
                                        hatcheryDashboard.setIncubatorEntry(incubatorEntry);
                                        hatcheryDashboard.setRefIncubatorEntry(incubatorEntry.getReference());
                                        hatcheryDashboard.setDatetimeIncubatorEntry(incubatorExit.getDateTimeIncubatorEntry());
                                        hatcheryDashboard.setDateTimeReelIncubatorExit(incubatorExit.getDateTimeIncubatorExit());

                                        hatcheryDashboard.setIncubatorExit(incubatorExit);
                                        hatcheryDashboard.setRefIncubatorExit(incubatorExit.getReference());
                                        hatcheryDashboard.setDatetimeIncubatorExit(incubatorExit.getDatetime());

                                        hatcheryDashboard.setHatcherEntry(hatcherEntry);
                                        hatcheryDashboard.setRefHatcherEntry(hatcherEntry.getReference());
                                        hatcheryDashboard.setDatetimeHatcherEntry(hatcherEntry.getDateTime());

                                        hatcheryDashboard.setHatcherExit(hatcherExit);
                                        hatcheryDashboard.setRefHatcherExit(hatcherExit.getReference());
                                        hatcheryDashboard.setDatetimeHatcherExit(hatcherExit.getDatetime());
                                        hatcheryDashboard.setSafe(hatcherExit.getSafe());
                                        hatcheryDashboard.setSuccessRate(hatcherExit.getSuccessRate());
                                        if (hatcherExit.getCustomerSaleOrderLine() != null && !hatcherExit.getCustomerSaleOrderLine().isEmpty()) {
                                            hatcheryDashboard.setSaleOrder(hatcherExit.getCustomerSaleOrderLine().get(0).getSaleOrder());
                                        }
                                        hatcheryDashboard.setInternNumber(hatcherExit.getInternNumber());
                                        hatcheryDashboard.setExternNumber(hatcherExit.getExternNumber());
                                        hatcheryDashboard.setRemainingNumberOrder(hatcherExit.getSafe() - (hatcherExit.getSort() + hatcherExit.getMortality()));
                                        hatcheryDashboard.setHatchery(hatcherExit.getHatchery());

                                        hatcheryDashboard.setLineNumber(incubatorsLoadingProgramLine.getLineNumber());
                                        hatcheryDashboard.setIncubationDatetime(incubatorsLoadingProgramLine.getIncubationDatetime());
                                        hatcheryDashboard.setTransferDatetime(incubatorsLoadingProgramLine.getTransferDatetime());
                                        hatcheryDashboard.setHatchingDatetime(incubatorsLoadingProgramLine.getHatchingDatetime());
                                        hatcheryDashboard.setCompleteReference(incubatorsLoadingProgramLine.getCompleteReference());
                                        hatcheryDashboard.setEstimatedNumberOfChicks(incubatorsLoadingProgramLine.getEstimatedNumberOfChicks());
                                        hatcheryDashboard.setStatus(incubatorsLoadingProgramLine.getStatus());

                                        hatcheryDashboard.setIncubator(hatcherExit.getIncubator());
                                        hatcheryDashboard.setRefIncubator(hatcherExit.getIncubator().getReference());

                                        int sumOfDeliveredNumber = hatcheryDeliverySale.getHatcheryDeliverySaleList().stream()
                                                .mapToInt(line -> line.getReelDeliveredQty().intValue())
                                                .sum();

                                        int sumOfRemainingNumber = hatcheryDeliverySale.getHatcheryDeliverySaleList().stream()
                                                .mapToInt(line -> line.getRemainingQty().intValue())
                                                .sum();

                                        hatcheryDashboard.setCommercial(sumOfDeliveredNumber);
                                        hatcheryDashboard.setRemainingNumberDelivery(sumOfRemainingNumber);

                                        hatcheryDashboardList.add(hatcheryDashboard);
                                        System.out.println(hatcheryDashboard);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        response.setValue("hatcheryDashboardList", hatcheryDashboardList);

    }


    public void getLinesFleshPrograms(ActionRequest request, ActionResponse response) throws AxelorException {

        List<FleshLoadingProgramLine> fleshLoadingProgramLineArrayList = new ArrayList<>();
        List<FleshLoadingProgramLine> FleshLoadingProgramLineList;


        String startLoadingDatetime =  request.getContext().get("startLoadingDatetime").toString();
        String endLoadingDatetime =  request.getContext().get("endLoadingDatetime").toString();

        if(request.getContext().get("status1") == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No status was selected ! please select at least 1 status"));
        }

        String selectedStatuses =  request.getContext().get("status1").toString();
        List<Integer> selectedStatuses1 = Arrays.stream(selectedStatuses.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date endDate = null;
        try {
            endDate = dateFormat.parse(endLoadingDatetime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date newEndDate = calendar.getTime();

        String formattedDate = dateFormat.format(newEndDate);



        if(request.getContext().get("center") == null ) {
             FleshLoadingProgramLineList = fleshLoadingProgramLineRepository.all().filter(
                            "self.plannedLoadingDatetime between ? and ? " +
                                    "AND self.status in :statuses", startLoadingDatetime , formattedDate)
                    .bind("statuses", selectedStatuses1)
                    .fetch();
        }
        else {

            String center =  request.getContext().get("center").toString();
            Map<String, Object> centerMap = (Map<String, Object>) request.getContext().get("center");
            Integer centerId = (Integer) centerMap.get("id");
            FleshLoadingProgramLineList = fleshLoadingProgramLineRepository.all().filter(
                            "self.fleshLoadingProgram.center = (select c from Center c where c.id = ?)" +
                                    "AND self.plannedLoadingDatetime between ? and ? " +
                                    "AND self.status in :statuses", centerId, startLoadingDatetime , formattedDate)
                    .bind("statuses", selectedStatuses1)
                    .fetch();
        }

         for (FleshLoadingProgramLine fleshLoadingProgramLine : FleshLoadingProgramLineList) {

             FleshLoadingProgramLine fleshLoadingProgram1 = new FleshLoadingProgramLine();

             fleshLoadingProgram1.setLineNumber(fleshLoadingProgramLine.getLineNumber());
             fleshLoadingProgram1.setBreed(fleshLoadingProgramLine.getBreed());
             fleshLoadingProgram1.setBreedingShed(fleshLoadingProgramLine.getBreedingShed());
             fleshLoadingProgram1.setPlannedLoadingDatetime(fleshLoadingProgramLine.getPlannedLoadingDatetime());
             fleshLoadingProgram1.setRealLoadingDatetime(fleshLoadingProgramLine.getRealLoadingDatetime());
             fleshLoadingProgram1.setNumberOfChicksPerCage(fleshLoadingProgramLine.getNumberOfChicksPerCage());
             fleshLoadingProgram1.setNumberOfChicks(fleshLoadingProgramLine.getNumberOfChicks());
             fleshLoadingProgram1.setAge(fleshLoadingProgramLine.getAge());
             fleshLoadingProgram1.setPeriod(fleshLoadingProgramLine.getPeriod());
             fleshLoadingProgram1.setSlaughterFromDatetime(fleshLoadingProgramLine.getSlaughterFromDatetime());
             fleshLoadingProgram1.setSlaughterToDatetime(fleshLoadingProgramLine.getSlaughterToDatetime());
             fleshLoadingProgram1.setStatus(fleshLoadingProgramLine.getStatus());
             fleshLoadingProgram1.setFleshLoadingProgram(fleshLoadingProgramLine.getFleshLoadingProgram());
             fleshLoadingProgram1.setCenter(fleshLoadingProgramLine.getBreedingShed().getCenter());

             fleshLoadingProgramLineArrayList.add(fleshLoadingProgram1);

        }

        response.setValue("fleshLoadingProgramLineList", fleshLoadingProgramLineArrayList);

    }

    public void getLinesReceiptEggsProd(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardReceiptEggProdChicks> dashboardReceiptEggProdChicks = new ArrayList<>();
        if(request.getContext().get("secondTrackingNumber") == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No tracking number was specified !"));
        }
        Map<String, Object> trackingNumberMap = (Map<String, Object>) request.getContext().get("secondTrackingNumber");
        Integer trackingNumberId = (Integer) trackingNumberMap.get("id");

        for (HatcheryEggsReceptionLine hatcheryEggsReceptionLine : hatcheryEggsReceptionLineRepository.all().filter("self.eggsReception.status = 2 AND self.eggsReception.trackingNumber.id = ?",trackingNumberId ).fetch()) {

            BreedingShed breedingShed = hatcheryEggsReceptionLine.getBreedingShed();
            TrackingNumber trackingNumber = hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber();

            Integer sumHe = hatcheryEggsReceptionLine.getReceivedNumberOfHe();
            Integer sumDye = hatcheryEggsReceptionLine.getReceivedNumberOfDye();
            Integer sumDe = hatcheryEggsReceptionLine.getReceivedNumberOfDe();
            Integer sumEggs = 0;
            Integer sumHatcherEntry=0;
            Integer sumHatchingSafeChicks=0;
            BigDecimal safePer = BigDecimal.ZERO;

            for (IncubatorEntryLine incubatorEntryLine : incubatorEntryLineRepository.all().filter("self.trackingNumber = ? AND self.incubatorEntry.status = 2", hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber()).fetch()) {
                sumEggs += incubatorEntryLine.getEggsNumber();
            }
            for (HatcherEntryLine hatcherEntryLine : hatcherEntryLineRepository.all().filter("self.trackingNumber = ? AND self.hatcherEntry.status = 2", hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber()).fetch()) {
                sumHatcherEntry += hatcherEntryLine.getNumberOfOac();
            }
            for (HatcherExitLine hatcherExitLine : hatcherExitLineRepository.all().filter("self.trackingNumber = ? AND self.hatcherExit.status = 2", hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber()).fetch()) {
                sumHatchingSafeChicks += hatcherExitLine.getSafe();
                safePer = safePer.add(hatcherExitLine.getHatcherExit().getSafeRate()) ;
            }

            boolean found = false;
            List<HatcherExitLine> hatcherExitLines = hatcherExitLineRepository.all().filter("self.trackingNumber = ? AND self.hatcherExit.status = 2", hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber()).fetch();
            int count = hatcherExitLines.size();
            BigDecimal safeRate = count > 0 ? safePer.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            for (DashboardReceiptEggProdChicks dashboardReceipt : dashboardReceiptEggProdChicks) {
                if (dashboardReceipt.getBreedingShed().equals(breedingShed) && dashboardReceipt.getTrackingNumber().equals(trackingNumber)) {
                    dashboardReceipt.setHe(dashboardReceipt.getHe() + sumHe);
                    dashboardReceipt.setDye(dashboardReceipt.getDye() + sumDye);
                    dashboardReceipt.setDe(dashboardReceipt.getDe() + sumDe);
                    dashboardReceipt.setTotal(dashboardReceipt.getDe()+ dashboardReceipt.getDye() + dashboardReceipt.getHe());

                    dashboardReceipt.setPercentageHe(BigDecimal.valueOf((float)dashboardReceipt.getHe()/dashboardReceipt.getTotal()));
                    dashboardReceipt.setPercentageDe(BigDecimal.valueOf((float)dashboardReceipt.getDe()/dashboardReceipt.getTotal()));
                    dashboardReceipt.setPercentageDye(BigDecimal.valueOf((float)dashboardReceipt.getDye()/dashboardReceipt.getTotal()));

                    dashboardReceipt.setIncubatorLoading(dashboardReceipt.getIncubatorLoading() + sumEggs);
                    dashboardReceipt.setHatcherEntry(dashboardReceipt.getHatcherEntry() + sumHatcherEntry);
                    dashboardReceipt.setHatchingSafeChicks(dashboardReceipt.getHatchingSafeChicks() + sumHatchingSafeChicks);
                    dashboardReceipt.setHatchingPercentageSafe(safeRate);

                    found = true;
                    break;
                }
            }

            if (!found) {
                DashboardReceiptEggProdChicks dashboardReceipt = new DashboardReceiptEggProdChicks();
                dashboardReceipt.setBreedingShed(breedingShed);
                dashboardReceipt.setTrackingNumber(trackingNumber);
                dashboardReceipt.setHe(sumHe);
                dashboardReceipt.setDye(sumDye);
                dashboardReceipt.setDe(sumDe);
                dashboardReceipt.setTotal(sumHe+sumDye+sumDe);
                dashboardReceipt.setPercentageHe(BigDecimal.valueOf((float)sumHe / (sumHe + sumDye + sumDe)));
                dashboardReceipt.setPercentageDe(BigDecimal.valueOf((float)sumDe/(sumHe+sumDye+sumDe)));
                dashboardReceipt.setPercentageDye(BigDecimal.valueOf((float)sumDye/(sumHe+sumDye+sumDe)));

                dashboardReceipt.setIncubatorLoading(sumEggs);
                dashboardReceipt.setHatcherEntry(sumHatcherEntry);
                dashboardReceipt.setHatchingSafeChicks(sumHatchingSafeChicks);
                dashboardReceipt.setHatchingPercentageSafe(safeRate);

                dashboardReceiptEggProdChicks.add(dashboardReceipt);
            }
        }


        response.setValue("receiptEggsProdChicksList", dashboardReceiptEggProdChicks);
    }




}

