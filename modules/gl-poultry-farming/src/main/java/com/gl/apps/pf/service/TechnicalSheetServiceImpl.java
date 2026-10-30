package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

public class TechnicalSheetServiceImpl implements TechnicalSheetService{
    @Inject private TechnicalSheetLineRepository technicalSheetLineRepository;
    @Inject private TechnicalSheetRepository technicalSheetRepository;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private StockMoveLineRepository stockMoveLineRepository;
    @Inject private ProductOrderLineRepository productOrderLineRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public TechnicalSheet Validate(TechnicalSheet technicalSheet) throws AxelorException {

        TechnicalSheet technicalSheet1 = technicalSheetRepository.find(technicalSheet.getId());

        if (technicalSheet1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if (!technicalSheet1.getMaleTechnicalSheetLineList().isEmpty()) {
            for (TechnicalSheetLine line : technicalSheet1.getMaleTechnicalSheetLineList()) {
                if (line != null) {
                    ValidateLine(line);
                }
            }
        }

        if (!technicalSheet1.getFemaleTechnicalSheetLineList().isEmpty()) {
            for (TechnicalSheetLine line : technicalSheet1.getFemaleTechnicalSheetLineList()) {
                if (line != null) {
                    ValidateLine(line);
                }
            }
        }
        technicalSheet1.setStatus(TechnicalSheetRepository.STATUS_FINALIZED_SHEET);

        return technicalSheetRepository.save(technicalSheet1);
    }

    @Transactional
    public TechnicalSheetLine test(TechnicalSheetLine technicalSheetLine) throws AxelorException {
        TechnicalSheetLine technicalSheet = technicalSheetLineRepository.find(technicalSheetLine.getId());

        if (technicalSheet.getTechnicalSheet() != null && technicalSheet.getTechnicalSheet().getBreed() != null) {
            for (BroilerGuide x : technicalSheet.getTechnicalSheet().getBreed().getBroilerGuideList()) {
                if (Objects.equals(technicalSheet.getTechnicalSheet().getDailyAge(), x.getDayNumber())){
                    technicalSheet.setStandardWeight(BigDecimal.valueOf(x.getWeight()));
                    technicalSheet.setStandardConsumptionIndex(x.getiCIndicator());
                    technicalSheet.setTemperatureConsg(x.getSetTemperature());
                }
            }
        }
        return technicalSheet;
    }
    @Transactional
    public TechnicalSheetLine ValidateLine(TechnicalSheetLine technicalSheetLine) throws AxelorException {
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        int sumMaleReformNumber = 0;
        int sumFemaleReformNumber = 0;
        int sumTransferNumber = 0;
        int sumOutMovedNumber = 0;
        int sumInMovedNumber = 0;

        TechnicalSheetLine technicalSheet = technicalSheetLineRepository.find(technicalSheetLine.getId());
        StockLocation virtualConsumptionLocation = app.getVirtualConsumptionLocation();


        if (technicalSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if (technicalSheet.getBreedingShed() == null) {
            throw new AxelorException(
                    technicalSheet,
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND),
                    technicalSheet.getReference());
        }
        // calculate sum of transfer number
        for (ElevTransferLine elevTransferLine : technicalSheet.getElevTransferLineList()) {
            sumTransferNumber += elevTransferLine.getToTransferChickensNumber();
        }
        // calculate sum of out displacement number => -
        for (StartElevMovement elevMovement : technicalSheet.getElevMovementList()) {
            for (StartElevMovementLine line : elevMovement.getStartElevMovementLineList()) {
                sumOutMovedNumber += line.getMovedNumber();
            }
        }

        // calculate sum of in displacement number => +
        for (StartElevMovementLine elevMovement : technicalSheet.getElevMovementLineList()) {
            sumInMovedNumber += elevMovement.getMovedNumber();
        }

        //calculate sum of reform number
        for (ElevReform elevReform : technicalSheet.getElevReformList()) {
            sumMaleReformNumber += elevReform.getMaleReformNumber();
            sumFemaleReformNumber+= elevReform.getFemaleReformNumber();
        }


        TechnicalSheetLine stsParent = technicalSheet.getPreviousTechnicalSheet();
        technicalSheet.setCumulatedMortality((stsParent == null ? 0 : stsParent.getCumulatedMortality()) + technicalSheet.getMaleMortality() + technicalSheet.getFemaleMortality());
        int sum = technicalSheet.getBreedingShed().getReceivedFemalesNumber() + technicalSheet.getBreedingShed().getReceivedMalesNumber();
        if(sum != 0) {
            float cm = technicalSheet.getCumulatedMortality();
            float rm = cm / sum * 100;
            BigDecimal rateMortality = new BigDecimal(rm).setScale(2, RoundingMode.HALF_UP);
            technicalSheet.setRateMortality(rateMortality);
        }
        else {
            technicalSheet.setRateMortality(BigDecimal.ZERO);
        }


        // CASE 1 : TECHNICAL SHEET FOR STARTUP : Breeding shed contains females
        if (technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0 && technicalSheet.getBreedingShed().getReceivedMalesNumber() == 0) {
            // female chicks

            StockMove dispatcherMove = CreateOrUpdateStockMove(technicalSheet);

            for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
                if(AuthUtils.getUser().getActiveCompany() != null ) {
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
                else throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.ACTIVE_COMPANY_NOT_FOUND));
            }
            for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
                if(AuthUtils.getUser().getActiveCompany() != null ) {
                    if (!line.getProduct().getAllowNegativeStock()) {
                        BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(dispatcherMove.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + line.getProductName() + " !"));
                        }
                    }
                }
                else throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.ACTIVE_COMPANY_NOT_FOUND));
            }
            dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
            if(technicalSheet.getTechnicalSheet() != null ) {
                dispatcherMove.setCurrentTrackingNumber(technicalSheet.getTechnicalSheet().getTrackingNumber());
                dispatcherMove.setCurrentDailyAge(technicalSheet.getTechnicalSheet().getDailyAge());
                dispatcherMove.setCurrentBreed(technicalSheet.getTechnicalSheet().getBreed());
            }
            technicalSheet.setDispatchStock(dispatcherMove);
            stockMoveService.plan(dispatcherMove);
            stockMoveService.realize(dispatcherMove);

            technicalSheet.setFemaleCumulatedLoss((stsParent == null ? 0 : stsParent.getFemaleCumulatedLoss()) + technicalSheet.getFemaleMortality() + technicalSheet.getFemaleTriOut());

            technicalSheet.setFemaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getFemaleMortality() - sumFemaleReformNumber - sumTransferNumber - sumOutMovedNumber + sumInMovedNumber )
                    : stsParent.getFemaleRemainingNumber() + sumInMovedNumber - technicalSheet.getFemaleMortality() - sumFemaleReformNumber - sumTransferNumber - sumOutMovedNumber));

            float femaleQtc = 1000 * technicalSheet.getFemaleConsumedProductQty().floatValue() /
                    (stsParent == null ? technicalSheet.getBreedingShed().getReceivedFemalesNumber() : stsParent.getFemaleRemainingNumber());

            technicalSheet.setFemaleConsumedProductQtyByChick(BigDecimal.valueOf(femaleQtc));

            float femaleCcpbc = (stsParent == null ? 0 : stsParent.getFemaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
            technicalSheet.setFemaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(femaleCcpbc));

            if(technicalSheet.getFemaleCumulatedConsumedProductQtyByChick().compareTo(BigDecimal.ZERO) == 0){
                technicalSheet.setFemaleWaterConsumptionByAliment(BigDecimal.ZERO);
            }
            else {
                float femaleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
                technicalSheet.setFemaleWaterConsumptionByAliment(BigDecimal.valueOf(femaleEa));
            }

            if(technicalSheet.getTechnicalSheet() != null && technicalSheet.getTechnicalSheet().getCenter().getType() == CenterRepository.TYPE_BROILER) {
                technicalSheet.getTechnicalSheet().getProductOrder().setStatus(ProductOrderRepository.STATUS_RESERVED);
                // Get Standard weight/CI/TemeratureCong from boilerGuide
                if (technicalSheet.getTechnicalSheet().getBreed() != null) {
                    for (BroilerGuide x : technicalSheet.getTechnicalSheet().getBreed().getBroilerGuideList()) {
                        if (Objects.equals(technicalSheet.getTechnicalSheet().getDailyAge(), x.getDayNumber())) {
                            technicalSheet.setStandardWeight(BigDecimal.valueOf(x.getWeight()));
                            technicalSheet.setStandardConsumptionIndex(x.getiCIndicator());
                            technicalSheet.setTemperatureConsg(x.getSetTemperature());
                        }
                    }
                }
                // Calculate darkness
                if (technicalSheet.getLightingFrom() != null && technicalSheet.getLightingTo() != null) {
                    technicalSheet.setDarkness((int) Duration.between(technicalSheet.getLightingFrom(), technicalSheet.getLightingTo()).toHours());
                }
                else {
                    technicalSheet.setDarkness(0);
                }

                //Calculate Gap/Gain weight
                if (technicalSheet.getStandardWeight().compareTo(BigDecimal.ZERO) != 0 && technicalSheet.getFemaleWeight() != 0) {
                    technicalSheet.setGapWeight(BigDecimal.valueOf(technicalSheet.getFemaleWeight()).subtract(technicalSheet.getStandardWeight()));
                    technicalSheet.setGainWeight(stsParent == null ? BigDecimal.valueOf(technicalSheet.getFemaleWeight()) : BigDecimal.valueOf(technicalSheet.getFemaleWeight()).subtract(BigDecimal.valueOf(stsParent.getFemaleWeight())));
                }
                else {
                    technicalSheet.setGapWeight(BigDecimal.ZERO);
                    technicalSheet.setGainWeight(BigDecimal.ZERO);
                }

                if (technicalSheet.getNumberSlaughteredChicks() != 0) {
                    technicalSheet.setNumberSlaughteredCumulated((stsParent == null ? 0 : stsParent.getNumberSlaughteredCumulated()) + technicalSheet.getNumberSlaughteredChicks());
                }
                else {
                    technicalSheet.setNumberSlaughteredCumulated(0);
                }

                // Calculate stock Qte
                BigDecimal realQte = BigDecimal.ZERO;
                if (app != null && app.getChairChicks() != null) {
                    for (StockMoveLine x : stockMoveLineRepository.all().filter("self.product = ?1", app.getChairChicks()).fetch()) {
                        if (x.getRealQty() != null) {
                            realQte = realQte.add(x.getRealQty());
                        }
                    }
                }
                technicalSheet.setProductQteStock(realQte);

                // Calculate cumul/recept qte
                if (technicalSheet.getTechnicalSheet() != null && technicalSheet.getTechnicalSheet().getProductOrder() != null) {
                    BigDecimal receivedQty = BigDecimal.ZERO;
                    for (ProductOrderLine x : productOrderLineRepository.all().filter("self.productOrder = ?1", technicalSheet.getTechnicalSheet().getProductOrder()).fetch()) {
                        if (x.getReelQuantityProduct() != null) {
                            receivedQty = receivedQty.add(x.getReelQuantityProduct());
                        }
                    }
                    technicalSheet.setReceptProduct(receivedQty);
                    float cumulatedRecept = (stsParent == null || stsParent.getCumulatedReceptProduct() == null ? 0 : stsParent.getCumulatedReceptProduct().floatValue()) + technicalSheet.getReceptProduct().floatValue();
                    technicalSheet.setCumulatedReceptProduct(BigDecimal.valueOf(cumulatedRecept));
                }
                else {
                    technicalSheet.setReceptProduct(BigDecimal.ZERO);
                }

            }

        }

        // CASE 2 : TECHNICAL SHEET FOR STARTUP : Breeding shed contains males
        if (technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0 && technicalSheet.getBreedingShed().getReceivedFemalesNumber() == 0) {

            StockMove dispatcherMove = CreateOrUpdateStockMove(technicalSheet);
            for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
                if(AuthUtils.getUser().getActiveCompany() != null ) {
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
                else throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.ACTIVE_COMPANY_NOT_FOUND));
            }
            dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
            if (technicalSheet.getTechnicalSheet() != null) {
                dispatcherMove.setCurrentTrackingNumber(technicalSheet.getTechnicalSheet().getTrackingNumber());
                dispatcherMove.setCurrentDailyAge(technicalSheet.getTechnicalSheet().getDailyAge());
                dispatcherMove.setCurrentBreed(technicalSheet.getTechnicalSheet().getBreed());
            }
            technicalSheet.setDispatchStock(dispatcherMove);
            stockMoveService.plan(dispatcherMove);
            stockMoveService.realize(dispatcherMove);

            technicalSheet.setMaleCumulatedLoss((stsParent == null ? 0 : stsParent.getMaleCumulatedLoss()) + technicalSheet.getMaleMortality() + technicalSheet.getMaleTriOut());

            technicalSheet.setMaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getMaleMortality() - sumTransferNumber - sumMaleReformNumber - sumOutMovedNumber + sumInMovedNumber)
                    : stsParent.getMaleRemainingNumber() - technicalSheet.getMaleMortality() - sumTransferNumber - sumMaleReformNumber -  sumOutMovedNumber + sumInMovedNumber));

            float maleQtc = 1000 * technicalSheet.getMaleConsumedProductQty().floatValue() /
                    (stsParent == null ? technicalSheet.getBreedingShed().getReceivedMalesNumber() : stsParent.getMaleRemainingNumber());

            technicalSheet.setMaleConsumedProductQtyByChick(BigDecimal.valueOf(maleQtc));

            float maleCcpbc = (stsParent == null ? 0 : stsParent.getMaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
            technicalSheet.setMaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(maleCcpbc));



            if(technicalSheet.getMaleCumulatedConsumedProductQtyByChick().compareTo(BigDecimal.ZERO) == 0){
                technicalSheet.setMaleWaterConsumptionByAliment(BigDecimal.ZERO);
            }
            else {
                float maleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
                technicalSheet.setMaleWaterConsumptionByAliment(BigDecimal.valueOf(maleEa));
            }
        }

        // CASE 3 : TECHNICAL SHEET FOR REPORODUCTION
        if (technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0 && technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0)
        {
            // male chicks
            StockMove dispatcherMove = CreateOrUpdateStockMove(technicalSheet);
            for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
                if(AuthUtils.getUser().getActiveCompany() != null ) {
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
                else throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.ACTIVE_COMPANY_NOT_FOUND));
            }
            dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_PRODUCTS);
            if(technicalSheet.getTechnicalSheet() != null ) {
                dispatcherMove.setCurrentTrackingNumber(technicalSheet.getTechnicalSheet().getTrackingNumber());
                dispatcherMove.setCurrentDailyAge(technicalSheet.getTechnicalSheet().getDailyAge());
                dispatcherMove.setCurrentBreed(technicalSheet.getTechnicalSheet().getBreed());
            }

            technicalSheet.setDispatchStock(dispatcherMove);
            stockMoveService.plan(dispatcherMove);
            stockMoveService.realize(dispatcherMove);

            technicalSheet.setMaleCumulatedLoss((stsParent == null ? 0 : stsParent.getMaleCumulatedLoss()) + technicalSheet.getMaleMortality() + technicalSheet.getMaleTriOut());

            technicalSheet.setMaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getMaleMortality() - sumMaleReformNumber)
                    : stsParent.getMaleRemainingNumber() - technicalSheet.getMaleMortality() - sumMaleReformNumber));

            float maleQtc = 1000 * technicalSheet.getMaleConsumedProductQty().floatValue() /
                    (stsParent == null ? technicalSheet.getBreedingShed().getReceivedMalesNumber() : stsParent.getMaleRemainingNumber());

            technicalSheet.setMaleConsumedProductQtyByChick(BigDecimal.valueOf(maleQtc));

            float maleCcpbc = (stsParent == null ? 0 : stsParent.getMaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
            technicalSheet.setMaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(maleCcpbc));

            if(technicalSheet.getMaleCumulatedConsumedProductQtyByChick().compareTo(BigDecimal.ZERO) == 0){
                technicalSheet.setMaleWaterConsumptionByAliment(BigDecimal.ZERO);
            }
            else {
                float maleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
                technicalSheet.setMaleWaterConsumptionByAliment(BigDecimal.valueOf(maleEa));
            }

            // female chicks

            technicalSheet.setFemaleCumulatedLoss((stsParent == null ? 0 : stsParent.getFemaleCumulatedLoss()) + technicalSheet.getFemaleMortality() + technicalSheet.getFemaleTriOut());

            technicalSheet.setFemaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getFemaleMortality() - sumFemaleReformNumber)
                    : stsParent.getFemaleRemainingNumber() - technicalSheet.getFemaleMortality() - sumFemaleReformNumber));

            float femaleQtc = 1000 * technicalSheet.getFemaleConsumedProductQty().floatValue() /
                    (stsParent == null ? technicalSheet.getBreedingShed().getReceivedFemalesNumber() : stsParent.getFemaleRemainingNumber());

            technicalSheet.setFemaleConsumedProductQtyByChick(BigDecimal.valueOf(femaleQtc));

            float femaleCcpbc = (stsParent == null ? 0 : stsParent.getFemaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
            technicalSheet.setFemaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(femaleCcpbc));

            if(technicalSheet.getFemaleCumulatedConsumedProductQtyByChick().compareTo(BigDecimal.ZERO) == 0){
                technicalSheet.setFemaleWaterConsumptionByAliment(BigDecimal.ZERO);
            }
            else {
                float femaleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
                technicalSheet.setFemaleWaterConsumptionByAliment(BigDecimal.valueOf(femaleEa));

            }
        }

        if (technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0) {
            technicalSheet.setFemalesNumber(technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getFemaleCumulatedLoss());
            technicalSheet.getBreedingShed().setFemalesNumber(technicalSheet.getFemalesNumber());

        }
        if (technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0) {
            technicalSheet.setMalesNumber(technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getMaleCumulatedLoss());
            technicalSheet.getBreedingShed().setMalesNumber(technicalSheet.getMalesNumber());
        }

        if (technicalSheet.getFemaleWeight() > 0 && technicalSheet.getFemaleCumulatedConsumedProductQtyByChick() != null && technicalSheet.getFemaleCumulatedConsumedProductQtyByChick().compareTo(BigDecimal.ZERO) > 0) {
            technicalSheet.setActualConsumptionIndex(technicalSheet.getFemaleCumulatedConsumedProductQtyByChick().divide(BigDecimal.valueOf(technicalSheet.getFemaleWeight()), 3, RoundingMode.HALF_UP));
        }

        technicalSheet.setStatus(TechnicalSheetLineRepository.STATUS_FINALIZED_SHEET);
        technicalSheet.getBreedingShed().setLastTechnicalSheetLine(technicalSheet);
        technicalSheet = technicalSheetLineRepository.save(technicalSheet);

        return technicalSheet;
    }

//    @Override
//    public TechnicalSheetLine getDatePreviousTechnicalSheet(TechnicalSheetLine technicalSheet) throws AxelorException {
//        technicalSheet.getId();
//
//        Optional<User> usr = technicalSheet.getBreedingShed()
//                .getResponsibles().stream().filter(t-> !t.getBlocked()
//                        && ( t.getActivateOn() == null || technicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
//                        && ( t.getExpiresOn() == null || technicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
//                ).findFirst();
//
////        usr.ifPresent(technicalSheet::setCurrentResponsible);
//        TechnicalSheet parentTechnicalSheet = technicalSheet.getTechnicalSheet();
//        TrackingNumber nbr = parentTechnicalSheet.getTrackingNumber();
//        Breed breed = parentTechnicalSheet.getBreed();
//
////        TrackingNumber trackingNumber;
////        Breed breed;
////        if(technicalSheet.getTechnicalSheet().getTrackingNumber() != null && technicalSheet.getTechnicalSheet().getBreed() != null){
////            trackingNumber = technicalSheet.getTechnicalSheet().getTrackingNumber();
////            breed = technicalSheet.getTechnicalSheet().getBreed();
////        }
//
//
//        Comparator<TechnicalSheetLine> byDate = (i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime());
//        //Comparator<TechnicalSheetLine> byDate = Comparator.comparing(TechnicalSheetLine::getDateTime);
//
//        Optional<TechnicalSheetLine> stsParent = technicalSheetLineRepository.all()
//                .fetchStream()
//                .filter(t -> t.getBreedingShed() == technicalSheet.getBreedingShed()
//                        && Objects.equals(t.getId(), technicalSheet.getId())
//                        && t.getTechnicalSheet().getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber()
//                        && t.getTechnicalSheet().getBreed() == technicalSheet.getTechnicalSheet().getBreed()
//                        && t.getStatus() != TechnicalSheetLineRepository.STATUS_CANCELED
//                ).max(byDate);
//
//        if(stsParent.isPresent()){
//            if(stsParent.get().getStatus() != TechnicalSheetLineRepository.STATUS_FINALIZED_SHEET )
//            {
//                throw new AxelorException(
//                        technicalSheet,
//                        TraceBackRepository.CATEGORY_MISSING_FIELD,
//                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
//                        technicalSheet.getReference());
//            }
//            technicalSheet.setPreviousTechnicalSheet(stsParent.get());
//            technicalSheet.getTechnicalSheet().setDailyAge(stsParent.get().getTechnicalSheet().getDailyAge() + 1);
//            technicalSheet.setDateTime(stsParent.get().getDateTime().plusDays(1));
//            technicalSheet.getTechnicalSheet().setTrackingNumber(technicalSheet.getBreedingShed().getCurrentBatchNumber());
//            technicalSheet.getTechnicalSheet().setBreed(technicalSheet.getBreedingShed().getBreed());
//            technicalSheet.setMalesNumber(technicalSheet.getBreedingShed().getMalesNumber());
//            technicalSheet.setFemalesNumber(technicalSheet.getBreedingShed().getFemalesNumber());
//        }
//        else {
//            technicalSheet.getTechnicalSheet().setDailyAge(technicalSheet.getBreedingShed().getDailyAge());
//            technicalSheet.setDateTime(technicalSheet.getBreedingShed().getFillingDate().toLocalDate());
//
//            technicalSheet.getTechnicalSheet().setTrackingNumber(technicalSheet.getBreedingShed().getCurrentBatchNumber());
//            technicalSheet.getTechnicalSheet().setBreed(technicalSheet.getBreedingShed().getBreed());
//            technicalSheet.setMalesNumber(technicalSheet.getBreedingShed().getMalesNumber());
//            technicalSheet.setFemalesNumber(technicalSheet.getBreedingShed().getFemalesNumber());
//        }
//        return technicalSheet;
//    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(TechnicalSheetLine technicalSheetLine) throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (technicalSheetLine == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_LINE_NOT_FOUND));
        }

        TechnicalSheetLine technicalSheetLine1 = technicalSheetLineRepository.find(technicalSheetLine.getId());

        if (technicalSheetLine1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_LINE_NOT_FOUND));
        }

        StockLocation virtualConsumptionLocation = app.getVirtualConsumptionLocation();

        StockMove stockMove;

        if (technicalSheetLine1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(technicalSheetLine1.getBreedingShed().getCenter().getUnit().getCompany() != null) {
                values.put("company", technicalSheetLine1.getBreedingShed().getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if(technicalSheetLine1.getBreedingShed().getStockLocation() != null ){
                values.put("fromStockLocation", technicalSheetLine1.getBreedingShed().getStockLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed : " + technicalSheetLine1.getBreedingShed().getName() + " !"));
            }
            if(virtualConsumptionLocation != null ){
                values.put("toStockLocation", virtualConsumptionLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }
            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(technicalSheetLine1.getBreedingShed().getCenter());
            stockMove.setTechnicalSheetLine(technicalSheetLine1);
        }
        else {
            stockMove = technicalSheetLine1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }


        if (technicalSheetLine1.getMaleConsumedProductQty().compareTo(BigDecimal.ZERO) > 0) {
            StockMoveLine sm1 = new StockMoveLine();
            sm1.setProduct(technicalSheetLine1.getMaleConsumedProduct());
            sm1.setQty(BigDecimal.valueOf(technicalSheetLine1.getMaleConsumedProductQty().floatValue()));
            sm1.setRealQty(BigDecimal.valueOf(technicalSheetLine1.getMaleConsumedProductQty().floatValue()));

            if (technicalSheetLine1.getBreedingShed().getStockLocation() != null) {
                sm1.setFromStockLocation(technicalSheetLine1.getBreedingShed().getStockLocation());
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed :" + technicalSheetLine1.getBreedingShed().getName() + " !"));

            }
            if (virtualConsumptionLocation != null) {
                sm1.setToStockLocation(virtualConsumptionLocation);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }
            sm1.setProductName(sm1.getProduct().getName());
            sm1.setUnit(sm1.getProduct().getUnit());
            if(technicalSheetLine1.getTechnicalSheet() != null ) {
                sm1.setTrackingNumber(technicalSheetLine1.getTechnicalSheet().getTrackingNumber());
            }
            stockMove.addStockMoveLineListItem(sm1);
        }
        if (technicalSheetLine1.getFemaleConsumedProductQty().compareTo(BigDecimal.ZERO) > 0) {
            StockMoveLine sm2 = new StockMoveLine();
            sm2.setProduct(technicalSheetLine1.getFemaleConsumedProduct());
            sm2.setQty(BigDecimal.valueOf(technicalSheetLine1.getFemaleConsumedProductQty().floatValue()));
            sm2.setRealQty(BigDecimal.valueOf(technicalSheetLine1.getFemaleConsumedProductQty().floatValue()));
            if (technicalSheetLine1.getBreedingShed().getStockLocation() != null) {
                sm2.setFromStockLocation(technicalSheetLine1.getBreedingShed().getStockLocation());
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed :" + technicalSheetLine1.getBreedingShed().getName() + " !"));

            }
            if (virtualConsumptionLocation != null) {
                sm2.setToStockLocation(virtualConsumptionLocation);
            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }
            sm2.setProductName(sm2.getProduct().getName());
            sm2.setUnit(sm2.getProduct().getUnit());
            if (technicalSheetLine1.getTechnicalSheet() != null) {
                sm2.setTrackingNumber(technicalSheetLine1.getTechnicalSheet().getTrackingNumber());
            }
            stockMove.addStockMoveLineListItem(sm2);
    }

        return stockMoveRepo.save(stockMove);
    }
}




