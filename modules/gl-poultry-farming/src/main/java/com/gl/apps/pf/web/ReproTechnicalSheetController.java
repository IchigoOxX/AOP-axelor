package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.PharmacyTreatmentLine;
import com.gl.apps.pf.db.ReproElevReform;
import com.gl.apps.pf.db.ReproTechnicalSheet;
import com.gl.apps.pf.db.StartTechnicalSheet;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ReproTechnicalSheetService;
import com.gl.apps.pf.service.StartTechnicalSheetService;
import com.google.inject.Inject;

import java.util.Comparator;
import java.util.Optional;

public class ReproTechnicalSheetController {
    @Inject
    private ReproTechnicalSheetRepository reproTechnicalSheetRepository;
    @Inject
    private ReproTechnicalSheetService reproTechnicalSheetService;
    @Inject
    private ReproElevReformRepository reproElevReformRepository;
    @Inject
    private PharmacyTreatmentLineRepository pharmacyTreatmentLineRepo;

    public void getDateReproTechnicalSheet(ActionRequest request, ActionResponse response)
            throws AxelorException {

        ReproTechnicalSheet reproTechnicalSheet = request.getContext().asType(ReproTechnicalSheet.class);
        if (reproTechnicalSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if (reproTechnicalSheet.getBreedingShed() == null) {
            throw new AxelorException(
                    reproTechnicalSheet,
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND),
                    reproTechnicalSheet.getReference());
        }

        Optional<User> usr = reproTechnicalSheet.getBreedingShed()
                .getResponsibles().stream().filter(t-> !t.getBlocked()
                        && ( t.getActivateOn() == null || reproTechnicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
                        && ( t.getExpiresOn() == null || reproTechnicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
                ).findFirst();

        usr.ifPresent(reproTechnicalSheet::setCurrentResponsible);

        Comparator<ReproTechnicalSheet> byDate =
                (i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime());
        Optional<ReproTechnicalSheet> stsParent = reproTechnicalSheetRepository.all()
                .fetchStream()
                .filter(t -> t.getBreedingShed() == reproTechnicalSheet.getBreedingShed()
                        && t.getTrackingNumber() == reproTechnicalSheet.getTrackingNumber()
                        && t.getBreed() == reproTechnicalSheet.getBreed()
                        && t.getStatus() != StartTechnicalSheetRepository.STATUS_CANCELED
                ).max(byDate);

        if(stsParent.isPresent()){
            if(stsParent.get().getStatus() != StartTechnicalSheetRepository.STATUS_FINALIZED_SHEET )
            {
                throw new AxelorException(
                        reproTechnicalSheet,
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
                        reproTechnicalSheet.getReference());
            }
            reproTechnicalSheet.setPreviousTechnicalSheet(stsParent.get());
            reproTechnicalSheet.setDailyAge(stsParent.get().getDailyAge() + 1);
            reproTechnicalSheet.setDateTime(stsParent.get().getDateTime().plusDays(1));
            reproTechnicalSheet.setTrackingNumber(reproTechnicalSheet.getBreedingShed().getCurrentBatchNumber());
            reproTechnicalSheet.setBreed(reproTechnicalSheet.getBreedingShed().getBreed());
            reproTechnicalSheet.setMalesNumber(reproTechnicalSheet.getBreedingShed().getMalesNumber());
            reproTechnicalSheet.setFemalesNumber(reproTechnicalSheet.getBreedingShed().getFemalesNumber());
        }
        else {
            reproTechnicalSheet.setDailyAge(reproTechnicalSheet.getBreedingShed().getDailyAge());
            reproTechnicalSheet.setDateTime(reproTechnicalSheet.getBreedingShed().getFillingDate().toLocalDate());

            reproTechnicalSheet.setTrackingNumber(reproTechnicalSheet.getBreedingShed().getCurrentBatchNumber());
            reproTechnicalSheet.setBreed(reproTechnicalSheet.getBreedingShed().getBreed());
            reproTechnicalSheet.setMalesNumber(reproTechnicalSheet.getBreedingShed().getMalesNumber());
            reproTechnicalSheet.setFemalesNumber(reproTechnicalSheet.getBreedingShed().getFemalesNumber());
        }

        response.setValues(reproTechnicalSheet);
    }

    public void reproTechnicalSheetValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproTechnicalSheet reproTechnicalSheet = request.getContext().asType(ReproTechnicalSheet.class);

        if (reproTechnicalSheet != null ) {
            reproTechnicalSheetService.Validate(reproTechnicalSheet);
        }

        response.setValues(reproTechnicalSheet);
        response.setReload(true);
    }

    public void getReproElevReformList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproTechnicalSheet technicalSheet = request.getContext().asType(ReproTechnicalSheet.class);
        if (technicalSheet != null) {
            technicalSheet.clearElevReformList();
            for (ReproElevReform x : reproElevReformRepository.all().fetch()) {
                if (x.getReproTechnicalSheet() == null
                        && x.getStatus() == StartElevReformRepository.STATUS_EXECUTING
                        && x.getBreedingShed() == technicalSheet.getBreedingShed()
                        && x.getTrackingNumber() == technicalSheet.getTrackingNumber())
                {
                    x.setReproTechnicalSheet(technicalSheet);
                    technicalSheet.setMaleReform(x.getMaleReformNumber());
                    technicalSheet.setFemaleReform(x.getFemaleReformNumber());
                    technicalSheet.addElevReformListItem(x);
                }
            }
        }
        response.setValues(technicalSheet);
    }
    public void getPharmacyTreatmentsLineList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproTechnicalSheet technicalSheet = request.getContext().asType(ReproTechnicalSheet.class);

        if (technicalSheet != null) {
            technicalSheet.clearPharmacyTreatmentLineList();

            for (PharmacyTreatmentLine x : pharmacyTreatmentLineRepo.all().fetch()) {
                if (x.getReproTechnicalSheet() == null
                        && x.getPharmacyTreatment().getTrackingNumber() == technicalSheet.getTrackingNumber()
                        && x.getPharmacyTreatment().getStatus() == PharmacyTreatmentRepository.STATUS_EXECUTING
                        && x.getPharmacyTreatment().getBreedingShed() == technicalSheet.getBreedingShed()
                        && x.getPharmacyTreatment().getPlannedDate().equals(technicalSheet.getDateTime()))
                {
                    x.setReproTechnicalSheet(technicalSheet);
                    technicalSheet.addPharmacyTreatmentLineListItem(x);
                    technicalSheet.setMedicalTreatment(x.getMedicalTreatment());
                }
            }

        }
        response.setValues(technicalSheet);
    }

}
