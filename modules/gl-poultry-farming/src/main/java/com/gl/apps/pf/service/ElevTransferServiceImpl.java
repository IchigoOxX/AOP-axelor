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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ElevTransferServiceImpl implements ElevTransferService{
    @Inject
    private ElevTransferRepository elevTransferRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject BreedingShedRepository breedingShedRepository;
    @Inject private  ReceptionReproProductionRepository receptionReproProductionRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public ElevTransfer Validate(ElevTransfer elevTransfer) throws AxelorException {
        ElevTransfer elevTran = elevTransferRepository.find(elevTransfer.getId());
        if (elevTran == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_FOUND));
        }
        StockMove stockMove;

        int sumOfToTransferChickensNumber = elevTran.getElevTransferLineList().stream()
                .mapToInt(line -> line.getToTransferChickensNumber())
                .sum();

        if(!elevTran.getElevTransferLineList().isEmpty()){
            elevTran.getElevTransferLineList().forEach(
                line -> {
                    int remaing =line.getRemainingChickensNumber();
                    BreedingShed b = breedingShedRepository.find(line.getBreedingShed().getId()) ;
                    b.setTransferDate(elevTran.getDateTime());
                    if(Objects.equals(elevTransfer.getGender(), "M")){
                        b.setMalesNumber(remaing);

                    }
                    else if(Objects.equals(elevTransfer.getGender(), "F")){
                        b.setFemalesNumber(remaing);

                    }
                    breedingShedRepository.save(b);
                }
            );
        }

        BreedingShed b1 = elevTran.getBreedingShed();

        if(Objects.equals(elevTransfer.getGender(), "M")) {
            b1.setTotalNumbers(elevTran.getBreedingShed().getTotalNumbers() + sumOfToTransferChickensNumber);
            b1.setMalesNumber(elevTran.getBreedingShed().getMalesNumber() + sumOfToTransferChickensNumber);
        }
        if(Objects.equals(elevTransfer.getGender(), "F")) {
            b1.setTotalNumbers(elevTran.getBreedingShed().getTotalNumbers() + sumOfToTransferChickensNumber);
            b1.setFemalesNumber(elevTran.getBreedingShed().getFemalesNumber() + sumOfToTransferChickensNumber);
        }
        breedingShedRepository.save(b1);


        StockMove dispatcherMove = CreateOrUpdateStockMove(elevTran);
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
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
        dispatcherMove.setCurrentTrackingNumber(elevTran.getTrackingNumber());
        dispatcherMove.setCurrentBreed(elevTran.getBreed());
        elevTran.setDispatchStock(dispatcherMove);
        stockMoveService.plan(dispatcherMove);
        elevTran.setStatus(ElevTransferRepository.STATUS_EXECUTING);

        // create reception effectif repro
        ReceptionReproProduction receptionReproProduction = new ReceptionReproProduction();
        receptionReproProduction.setElevTransfer(elevTran);
        receptionReproProduction.setDateTime(LocalDate.now());
        receptionReproProduction.setFromCenter(elevTran.getFromCenter());
        receptionReproProduction.setCenter(elevTran.getCenter());
        receptionReproProduction.setDateElevTransfer(elevTran.getDateTime());
        receptionReproProduction.setBreedingShed(elevTran.getBreedingShed());
        receptionReproProduction.setBreed(elevTran.getBreed());
        receptionReproProduction.setTrackingNumber(elevTran.getTrackingNumber());
        receptionReproProduction.setGender(elevTran.getGender());
        receptionReproProduction.setDailyAge(elevTran.getCenter().getCurrentDailyAge());
        receptionReproProduction.setWeeklyAge(elevTran.getCenter().getCurrentWeeklyAge());
        if(Objects.equals(elevTransfer.getGender(), "M") && elevTran.getBreedingShed().getLastTechnicalSheetLine() != null) {
            receptionReproProduction.setWeight(BigDecimal.valueOf(elevTran.getBreedingShed().getLastTechnicalSheetLine().getMaleWeight()));
        }

        if(Objects.equals(elevTransfer.getGender(), "F") && elevTran.getBreedingShed().getLastTechnicalSheetLine() != null) {
            receptionReproProduction.setWeight(BigDecimal.valueOf(elevTran.getBreedingShed().getLastTechnicalSheetLine().getFemaleWeight()));
        }

        // sum of to transfer chickens
        int toTransferChickensNumber = 0;
        BigDecimal averageCostNumber = BigDecimal.ZERO;
        BigDecimal globalCostNumber = BigDecimal.ZERO;
        String fromBreedingShed = "";
        BigDecimal globalWeight = BigDecimal.ZERO;

        if (elevTran != null && elevTran.getElevTransferLineList() != null && !elevTran.getElevTransferLineList().isEmpty()) {
            toTransferChickensNumber = elevTran.getElevTransferLineList().stream()
                    .filter(Objects::nonNull)
                    .map(ElevTransferLine::getToTransferChickensNumber)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();

            averageCostNumber = elevTran.getElevTransferLineList().stream()
                    .filter(Objects::nonNull)
                    .map(ElevTransferLine::getAverageCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            globalCostNumber = elevTran.getElevTransferLineList().stream()
                    .filter(Objects::nonNull)
                    .map(ElevTransferLine::getGlobalCost)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            globalWeight = elevTran.getElevTransferLineList().stream()
                    .filter(Objects::nonNull)
                    .map(ElevTransferLine::getCurrentWeight)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            fromBreedingShed = elevTran.getElevTransferLineList().stream()
                    .filter(Objects::nonNull)
                    .map(ElevTransferLine::getBreedingShed)
                    .filter(Objects::nonNull)
                    .map(BreedingShed::getName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }
        receptionReproProduction.setToTransferChickensNumber(toTransferChickensNumber);
        receptionReproProduction.setGlobalCost(globalCostNumber);
        receptionReproProduction.setAverageCost(averageCostNumber);
        receptionReproProduction.setFromBreedingShed(fromBreedingShed);
        receptionReproProduction.setWeight(globalWeight);
        receptionReproProduction.setStatus(ReceptionReproProductionRepository.STATUS_NOT_RECEIVED);
        receptionReproProductionRepository.save(receptionReproProduction);

        return elevTransferRepository.save(elevTran);
    }
    @Transactional
    public StockMove CreateOrUpdateStockMove(ElevTransfer elevTransfer)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (elevTransfer == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_FOUND));
        }

        ElevTransfer elevtr = elevTransferRepository.find(elevTransfer.getId());

        if (elevtr == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_SPECIFIED));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();

        StockLocation virtualStartupLocationLocation = app.getVirtualStartupLocation();
        StockLocation virtualProductionLocation = app.getVirtualProductionLocation();

        StockMove stockMove;

        if (elevtr.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(elevtr.getCenter().getUnit().getCompany() != null) {
                values.put("company", elevtr.getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if(virtualStartupLocationLocation != null ){
                values.put("fromStockLocation", virtualStartupLocationLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
            }
            if(virtualProductionLocation != null ){
                values.put("toStockLocation", virtualProductionLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }
            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(elevTransfer.getCenter());
            stockMove.setElevTransfer(elevTransfer);
        }
        else {
            stockMove = elevTransfer.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!elevtr.getElevTransferLineList().isEmpty()) {
            for (ElevTransferLine line : elevtr.getElevTransferLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getToTransferChickensNumber() > 0) {
                        if (line.getBreedingShed().getFemalesNumber() != 0) {
                            if (starterFemaleChick != null) {
                                sm.setProduct(starterFemaleChick);
                            } else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_CHICK_NOT_SPECIFIED));

                            }
                            sm.setQty(BigDecimal.valueOf(line.getToTransferChickensNumber()));
                            sm.setRealQty(BigDecimal.valueOf(line.getToTransferChickensNumber()));
                        } else if (line.getBreedingShed().getMalesNumber() != 0 || line.getBreedingShed().getReceivedMalesNumber() != 0 ) {
                            if (starterMaleChick != null) {
                                sm.setProduct(starterMaleChick);
                            } else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_CHICK_NOT_SPECIFIED));

                            }
                            sm.setQty(BigDecimal.valueOf(line.getToTransferChickensNumber()));
                            sm.setRealQty(BigDecimal.valueOf(line.getToTransferChickensNumber()));
                        }
                        if (line.getBreedingShed().getStockLocation() != null) {
                            sm.setFromStockLocation(line.getBreedingShed().getStockLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed :" + line.getBreedingShed().getName() + " !"));

                        }
                        if (elevTransfer.getBreedingShed().getStockLocation() != null) {
                            sm.setToStockLocation(elevTransfer.getBreedingShed().getStockLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed : " + elevTransfer.getBreedingShed().getName() + " !"));
                        }
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                        sm.setTrackingNumber(elevTransfer.getTrackingNumber());
                        stockMove.addStockMoveLineListItem(sm);
                    }
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public ElevTransfer Cancel(ElevTransfer elevTransfer) throws AxelorException {
        ElevTransfer elevTran = elevTransferRepository.find(elevTransfer.getId());
        if (elevTran == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_FOUND));
        }
        StockMove stockMove;

        int sumOfToTransferChickensNumber = elevTran.getElevTransferLineList().stream()
                .mapToInt(line -> line.getToTransferChickensNumber())
                .sum();

        if(!elevTran.getElevTransferLineList().isEmpty()){
            elevTran.getElevTransferLineList().forEach(
                line -> {
                    int remaing =line.getExistentNumber();
                    BreedingShed b = breedingShedRepository.find(line.getBreedingShed().getId());
                    b.setTransferDate(elevTran.getDateTime());
                    if(Objects.equals(elevTransfer.getGender(), "M")){
                        b.setMalesNumber(remaing);

                    }
                    else if(Objects.equals(elevTransfer.getGender(), "F")){
                        b.setFemalesNumber(remaing);

                    }
                    breedingShedRepository.save(b);
                }
            );
        }

        BreedingShed b1 = elevTran.getBreedingShed();

        if(Objects.equals(elevTransfer.getGender(), "M")) {
            b1.setTotalNumbers(elevTran.getBreedingShed().getTotalNumbers() - sumOfToTransferChickensNumber);
            b1.setMalesNumber(elevTran.getBreedingShed().getMalesNumber() - sumOfToTransferChickensNumber);
        }
        if(Objects.equals(elevTransfer.getGender(), "F")) {
            b1.setTotalNumbers(elevTran.getBreedingShed().getTotalNumbers() - sumOfToTransferChickensNumber);
            b1.setFemalesNumber(elevTran.getBreedingShed().getFemalesNumber() - sumOfToTransferChickensNumber);
        }

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = elevTran.getDispatchStock();
        if(dispatcherMove != null ){
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        elevTran.setStatus(ElevTransferRepository.STATUS_CANCELED);
        return elevTransferRepository.save(elevTran);

    }
}




