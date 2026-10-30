package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.StartTechnicalSheetRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StartTechnicalSheetServiceImp implements StartTechnicalSheetService {
    @Inject
    private StartTechnicalSheetRepository startTechnicalSheetRepository;
    @Inject
    private BreedingShedRepository breedingShedRepository;

    @Transactional
    public StartTechnicalSheet Validate(StartTechnicalSheet startTechnicalSheet) throws AxelorException {
        int sumReformNumber = 0;
        int sumOutMovedNumber = 0;
        int sumInMovedNumber = 0;
        int sumTransferNumber = 0;
        StartTechnicalSheet technicalSheet = startTechnicalSheetRepository.find(startTechnicalSheet.getId());
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
        StartTechnicalSheet stsParent = technicalSheet.getPreviousTechnicalSheet();

        technicalSheet.setCumulatedMortality((stsParent == null ? 0 : stsParent.getCumulatedMortality()) + technicalSheet.getMortality());
        int sum = technicalSheet.getBreedingShed().getReceivedFemalesNumber() + technicalSheet.getBreedingShed().getReceivedMalesNumber();
        float cm = technicalSheet.getCumulatedMortality();
        float rm = sum > 0 ? (cm / sum * 100) : 0;
        BigDecimal rateMortality = new BigDecimal(rm).setScale(2, RoundingMode.HALF_UP);
        technicalSheet.setRateMortality(rateMortality);

        technicalSheet.setCumulatedLoss((stsParent == null ? 0 : stsParent.getCumulatedLoss()) + technicalSheet.getMortality() + technicalSheet.getTriOut());

        float loss = technicalSheet.getCumulatedLoss();
        float rl = sum > 0 ? (loss / sum * 100) : 0;
        BigDecimal rateLoss = new BigDecimal(rl).setScale(2, RoundingMode.HALF_UP);
        technicalSheet.setRateLoss(rateLoss);

        // calculate sum of reform number
        if (technicalSheet.getElevReformList() != null) {
            for (StartElevReform elevReform : technicalSheet.getElevReformList()) {
                sumReformNumber += elevReform.getReformNumber();
            }
        }

        // calculate sum of out displacement number => -
        if (technicalSheet.getElevMovementList() != null) {
            for (StartElevMovement elevMovement : technicalSheet.getElevMovementList()) {
                if (elevMovement.getStartElevMovementLineList() != null) {
                    for (StartElevMovementLine line : elevMovement.getStartElevMovementLineList()) {
                        sumOutMovedNumber += line.getMovedNumber();
                    }
                }
            }
        }

        // calculate sum of in displacement number => +
        if (technicalSheet.getElevMovementLineList() != null) {
            for (StartElevMovementLine elevMovement : technicalSheet.getElevMovementLineList()) {
                sumInMovedNumber += elevMovement.getMovedNumber();
            }
        }

        // calculate sum of transfer number
        if (technicalSheet.getElevTransferLineList() != null) {
            for (ElevTransferLine elevTransferLine : technicalSheet.getElevTransferLineList()) {
                sumTransferNumber += elevTransferLine.getToTransferChickensNumber();
            }
        }

        int prevRemaining = stsParent == null ? sum : (stsParent.getRemainingNumber() != null ? stsParent.getRemainingNumber() : sum);
        int remainingNumber = prevRemaining + sumInMovedNumber - technicalSheet.getMortality() - sumReformNumber - sumOutMovedNumber - sumTransferNumber;
        technicalSheet.setRemainingNumber(remainingNumber);

        if (technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0) {
            technicalSheet.setFemalesNumber(technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getCumulatedLoss());
            technicalSheet.getBreedingShed().setFemalesNumber(technicalSheet.getFemalesNumber());
        }
        if (technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0) {
            technicalSheet.setMalesNumber(technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getCumulatedLoss());
            technicalSheet.getBreedingShed().setMalesNumber(technicalSheet.getMalesNumber());
        }

        float consumedQty = technicalSheet.getConsumedProductQty() != null ? technicalSheet.getConsumedProductQty().floatValue() : 0f;
        int denom = stsParent == null ? sum : (stsParent.getRemainingNumber() != null ? stsParent.getRemainingNumber() : sum);
        float qtc = denom > 0 ? (1000 * consumedQty / denom) : 0f;
        technicalSheet.setConsumedProductQtyByChick(BigDecimal.valueOf(qtc));

        float prevCcpbc = (stsParent == null || stsParent.getCumulatedConsumedProductQtyByChick() == null) ? 0f : stsParent.getCumulatedConsumedProductQtyByChick().floatValue();
        float ccpbc = prevCcpbc + technicalSheet.getConsumedProductQtyByChick().floatValue();
        technicalSheet.setCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(ccpbc));

        float water = technicalSheet.getWaterConsumption() != null ? technicalSheet.getWaterConsumption().floatValue() : 0f;
        float ea = qtc > 0 ? (water / qtc) : 0f;
        technicalSheet.setWaterConsumptionByAliment(BigDecimal.valueOf(ea));

        technicalSheet.setStatus(StartTechnicalSheetRepository.STATUS_FINALIZED_SHEET);
        technicalSheet.getBreedingShed().setLastStartTechnicalSheet(technicalSheet);
        technicalSheet = startTechnicalSheetRepository.save(technicalSheet);

        return technicalSheet;
    }
}
