package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ReproTechnicalSheetRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ReproTechnicalSheetServiceImpl implements ReproTechnicalSheetService {

    @Inject
    private ReproTechnicalSheetRepository reproTechnicalSheetRepository;
    @Inject
    private BreedingShedRepository breedingShedRepository;

    @Transactional
    public ReproTechnicalSheet Validate(ReproTechnicalSheet reproTechnicalSheet) throws AxelorException {
        int sumMaleReformNumber = 0;
        int sumFemaleReformNumber = 0;
        ReproTechnicalSheet technicalSheet = reproTechnicalSheetRepository.find(reproTechnicalSheet.getId());


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

        //calculate sum of reform number
        for (ReproElevReform elevReform : technicalSheet.getElevReformList()) {
            sumMaleReformNumber += elevReform.getMaleReformNumber();
            sumFemaleReformNumber+= elevReform.getFemaleReformNumber();
        }
        ReproTechnicalSheet stsParent = technicalSheet.getPreviousTechnicalSheet();


        technicalSheet.setCumulatedMortality((stsParent == null ? 0 : stsParent.getCumulatedMortality()) + technicalSheet.getMaleMortality() + technicalSheet.getFemaleMortality());
        int sum = technicalSheet.getBreedingShed().getReceivedFemalesNumber() + technicalSheet.getBreedingShed().getReceivedMalesNumber();
        float cm = technicalSheet.getCumulatedMortality();
        float rm = cm / sum * 100;
        BigDecimal rateMortality = new BigDecimal(rm).setScale(2, RoundingMode.HALF_UP);
        technicalSheet.setRateMortality(rateMortality);

        // male chicks
        technicalSheet.setMaleCumulatedLoss((stsParent == null ? 0 : stsParent.getMaleCumulatedLoss()) + technicalSheet.getMaleMortality() + technicalSheet.getMaleTriOut());

        technicalSheet.setMaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getMaleMortality() - sumMaleReformNumber)
                : stsParent.getMaleRemainingNumber() - technicalSheet.getMaleMortality() - sumMaleReformNumber));

        float maleQtc = 1000 * technicalSheet.getMaleConsumedProductQty().floatValue() /
                (stsParent == null ? technicalSheet.getBreedingShed().getReceivedMalesNumber() : stsParent.getMaleRemainingNumber());

        technicalSheet.setMaleConsumedProductQtyByChick(BigDecimal.valueOf(maleQtc));

        float maleCcpbc = (stsParent == null ? 0 : stsParent.getMaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
        technicalSheet.setMaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(maleCcpbc));


        float maleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getMaleConsumedProductQtyByChick().floatValue();
        technicalSheet.setMaleWaterConsumptionByAliment(BigDecimal.valueOf(maleEa));



        // female chicks
        technicalSheet.setFemaleCumulatedLoss((stsParent == null ? 0 : stsParent.getFemaleCumulatedLoss()) + technicalSheet.getFemaleMortality() + technicalSheet.getFemaleTriOut());

        technicalSheet.setFemaleRemainingNumber((stsParent == null ? (technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getFemaleMortality() - sumFemaleReformNumber)
                : stsParent.getFemaleRemainingNumber() - technicalSheet.getFemaleMortality() - sumFemaleReformNumber));

        float femaleQtc = 1000 * technicalSheet.getFemaleConsumedProductQty().floatValue() /
                (stsParent == null ? technicalSheet.getBreedingShed().getReceivedFemalesNumber() : stsParent.getFemaleRemainingNumber());

        technicalSheet.setFemaleConsumedProductQtyByChick(BigDecimal.valueOf(femaleQtc));

        float femaleCcpbc = (stsParent == null ? 0 : stsParent.getFemaleCumulatedConsumedProductQtyByChick().floatValue()) + technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
        technicalSheet.setFemaleCumulatedConsumedProductQtyByChick(BigDecimal.valueOf(femaleCcpbc));


        float femaleEa = technicalSheet.getWaterConsumption().floatValue() / technicalSheet.getFemaleConsumedProductQtyByChick().floatValue();
        technicalSheet.setFemaleWaterConsumptionByAliment(BigDecimal.valueOf(femaleEa));



        if (technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0) {
            technicalSheet.setFemalesNumber(technicalSheet.getBreedingShed().getReceivedFemalesNumber() - technicalSheet.getFemaleCumulatedLoss());
            technicalSheet.getBreedingShed().setFemalesNumber(technicalSheet.getFemalesNumber());

        }
        if (technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0) {
            technicalSheet.setMalesNumber(technicalSheet.getBreedingShed().getReceivedMalesNumber() - technicalSheet.getMaleCumulatedLoss());
            technicalSheet.getBreedingShed().setMalesNumber(technicalSheet.getMalesNumber());
        }

        technicalSheet.setStatus(ReproTechnicalSheetRepository.STATUS_FINALIZED_SHEET);
        technicalSheet.getBreedingShed().setLastRepoTechnicalSheet(technicalSheet);
        technicalSheet = reproTechnicalSheetRepository.save(technicalSheet);

        return technicalSheet;
    }
}
