package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.StartTechnicalSheetService;
import com.google.inject.Inject;

import java.util.Comparator;
import java.util.Optional;

public class StartTechnicalSheetController {
    @Inject
    private StartTechnicalSheetService startTechnicalSheetService;
    @Inject
    private StartElevReformRepository startElevReformRepository;
    @Inject
    private StartElevMovementRepository startElevMovementRepository;
    @Inject
    private StartElevMovementLineRepository startElevMovementLineRepository;
    @Inject
    private ElevTransferLineRepository elevTransferLineRepository;
    @Inject
    private StartTechnicalSheetRepository startTechnicalSheetRepo;
    @Inject
    private PharmacyTreatmentLineRepository pharmacyTreatmentLineRepo;

    public void getDateForTechnicalSheet(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet startTechnicalSheet = request.getContext().asType(StartTechnicalSheet.class);
        if (startTechnicalSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if (startTechnicalSheet.getBreedingShed() == null) {
            throw new AxelorException(
                    startTechnicalSheet,
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND),
                    startTechnicalSheet.getReference());
        }

        Optional<User> usr = startTechnicalSheet.getBreedingShed()
                .getResponsibles().stream().filter(t-> !t.getBlocked()
                        && ( t.getActivateOn() == null || startTechnicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
                        && ( t.getExpiresOn() == null || startTechnicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
                ).findFirst();

        usr.ifPresent(startTechnicalSheet::setCurrentResponsible);

        Comparator<StartTechnicalSheet> byDate =
                (i1, i2) -> i1.getDateTime().compareTo(i2.getDateTime());
        Optional<StartTechnicalSheet> stsParent = startTechnicalSheetRepo.all()
                .fetchStream()
                .filter(t -> t.getBreedingShed() == startTechnicalSheet.getBreedingShed()
                        && t.getTrackingNumber() == startTechnicalSheet.getTrackingNumber()
                        && t.getBreed() == startTechnicalSheet.getBreed()
                        && t.getStatus() != StartTechnicalSheetRepository.STATUS_CANCELED
                ).max(byDate);

        if(stsParent.isPresent()){
            if(stsParent.get().getStatus() != StartTechnicalSheetRepository.STATUS_FINALIZED_SHEET )
            {
                throw new AxelorException(
                        startTechnicalSheet,
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
                        startTechnicalSheet.getReference());
            }
            startTechnicalSheet.setPreviousTechnicalSheet(stsParent.get());
            startTechnicalSheet.setDailyAge(stsParent.get().getDailyAge() + 1);
            startTechnicalSheet.setDateTime(stsParent.get().getDateTime().plusDays(1));
            startTechnicalSheet.setTrackingNumber(startTechnicalSheet.getBreedingShed().getCurrentBatchNumber());
            startTechnicalSheet.setBreed(startTechnicalSheet.getBreedingShed().getBreed());
            startTechnicalSheet.setMalesNumber(startTechnicalSheet.getBreedingShed().getMalesNumber());
            startTechnicalSheet.setFemalesNumber(startTechnicalSheet.getBreedingShed().getFemalesNumber());
     }
        else {
            startTechnicalSheet.setDailyAge(startTechnicalSheet.getBreedingShed().getDailyAge());
            startTechnicalSheet.setDateTime(startTechnicalSheet.getBreedingShed().getFillingDate().toLocalDate());

            startTechnicalSheet.setTrackingNumber(startTechnicalSheet.getBreedingShed().getCurrentBatchNumber());
            startTechnicalSheet.setBreed(startTechnicalSheet.getBreedingShed().getBreed());
            startTechnicalSheet.setMalesNumber(startTechnicalSheet.getBreedingShed().getMalesNumber());
            startTechnicalSheet.setFemalesNumber(startTechnicalSheet.getBreedingShed().getFemalesNumber());
        }

      response.setValues(startTechnicalSheet);
    }
    public void technicalSheetValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet startTechnicalSheet = request.getContext().asType(StartTechnicalSheet.class);

        if (startTechnicalSheet != null ) {
            startTechnicalSheetService.Validate(startTechnicalSheet);
        }
        // check breeding shed null

        response.setValues(startTechnicalSheet);
        response.setReload(true);
    }

    public void getElevReformList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet technicalSheet = request.getContext().asType(StartTechnicalSheet.class);
        if (technicalSheet != null) {
            technicalSheet.clearElevReformList();
            for (StartElevReform x : startElevReformRepository.all().fetch()) {
                if (x.getStartTechnicalSheet() == null
                        && x.getStatus() == StartElevReformRepository.STATUS_EXECUTING
                        && x.getBreedingShed() == technicalSheet.getBreedingShed()
                        && x.getTrackingNumber() == technicalSheet.getTrackingNumber())
                {
                    x.setStartTechnicalSheet(technicalSheet);
                    technicalSheet.addElevReformListItem(x);
                    technicalSheet.setReform(x.getReformNumber());
                }
            }
        }
        response.setValues(technicalSheet);
    }


    public void getElevMovementList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet technicalSheet = request.getContext().asType(StartTechnicalSheet.class);
        if (technicalSheet != null) {
            technicalSheet.clearElevMovementList();
            int totalMovedNumber = 0;

            for (StartElevMovement x : startElevMovementRepository.all().fetch()) {
                if (x.getStartTechnicalSheet() == null
                        && x.getStatus() == StartElevMovementRepository.STATUS_EXECUTING
                        && x.getBreedingShed() == technicalSheet.getBreedingShed()
                        && x.getTrackingNumber() == technicalSheet.getTrackingNumber())
                {
                    x.setStartTechnicalSheet(technicalSheet);
                    technicalSheet.addElevMovementListItem(x);
                    totalMovedNumber += x.getStartElevMovementLineList().stream()
                            .mapToInt(line -> line.getMovedNumber())
                            .sum();
                }
            }
            technicalSheet.setDisplacementOut(totalMovedNumber);
        }
        response.setValues(technicalSheet);
    }


    public void getElevMovementLineList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet technicalSheet = request.getContext().asType(StartTechnicalSheet.class);
        if (technicalSheet != null) {
            technicalSheet.clearElevMovementLineList();
            for (StartElevMovementLine x : startElevMovementLineRepository.all().fetch()) {
                if (x.getTechnicalSheet() == null
                        && x.getElevMovement().getTrackingNumber() == technicalSheet.getTrackingNumber()
                        && x.getElevMovement().getStatus() == StartElevMovementRepository.STATUS_EXECUTING
                        && x.getBreedingShed() == technicalSheet.getBreedingShed())
                {
                    x.setTechnicalSheet(technicalSheet);
                    technicalSheet.addElevMovementLineListItem(x);
                    technicalSheet.setDisplacementIn(x.getMovedNumber());
                }
            }

        }
        response.setValues(technicalSheet);
    }


    public void getElevTransferLineList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet technicalSheet = request.getContext().asType(StartTechnicalSheet.class);

        if (technicalSheet != null) {
            technicalSheet.clearElevTransferLineList();

            for (ElevTransferLine x : elevTransferLineRepository.all().fetch()) {
                if (x.getStartTechnicalSheet() == null
                        && x.getElevTransfer().getTrackingNumber() == technicalSheet.getTrackingNumber()
                        && x.getElevTransfer().getStatus() == ElevTransferRepository.STATUS_RECEIVED
                        && x.getBreedingShed() == technicalSheet.getBreedingShed()
                        )
                {
                    x.setStartTechnicalSheet(technicalSheet);
                    technicalSheet.addElevTransferLineListItem(x);
                    technicalSheet.setTransfer(x.getToTransferChickensNumber());
                }
            }

        }
        response.setValues(technicalSheet);
    }
    public void getPharmacyTrLineList(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartTechnicalSheet technicalSheet = request.getContext().asType(StartTechnicalSheet.class);

        if (technicalSheet != null) {
            technicalSheet.clearPharmacyTreatmentLineList();

            for (PharmacyTreatmentLine x : pharmacyTreatmentLineRepo.all().fetch()) {
                if (x.getStartTechnicalSheet() == null
                        && x.getPharmacyTreatment().getTrackingNumber() == technicalSheet.getTrackingNumber()
                        && x.getPharmacyTreatment().getStatus() == PharmacyTreatmentRepository.STATUS_EXECUTING
                        && x.getPharmacyTreatment().getBreedingShed() == technicalSheet.getBreedingShed()
                        && x.getPharmacyTreatment().getPlannedDate().equals(technicalSheet.getDateTime())
                )
                {
                    x.setStartTechnicalSheet(technicalSheet);
                    technicalSheet.addPharmacyTreatmentLineListItem(x);
                    technicalSheet.setMedicalTreatment(x.getMedicalTreatment());
                }
            }

        }
        response.setValues(technicalSheet);
    }

}
