package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.TechnicalSheetService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TechnicalSheetController {
    @Inject private TechnicalSheetLineRepository technicalSheetLineRepository;
    @Inject private TechnicalSheetService technicalSheetService;
    @Inject private ElevTransferLineRepository elevTransferLineRepository;
    @Inject private StartElevMovementRepository startElevMovementRepository;
    @Inject private StartElevMovementLineRepository startElevMovementLineRepository;
    @Inject private PharmacyTreatmentLineRepository pharmacyTreatmentLineRepo;
    @Inject private ElevReformRepository elevReformRepository;
    @Inject  private TechnicalSheetRepository technicalSheetRepository;
    public void getLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {

        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);

        if (technicalSheet != null && technicalSheet.getCenter() != null) {
            technicalSheet.clearFemaleTechnicalSheetLineList();
            technicalSheet.clearMaleTechnicalSheetLineList();

            for (BreedingShed shed : technicalSheet.getCenter().getBreedingShedList()) {
                if (shed.getCenter() == technicalSheet.getCenter() && shed.getStatus() == BreedingShedRepository.STATUS_BUSY) {
                    TechnicalSheetLine line = new TechnicalSheetLine();
                    line.setBreedingShed(shed);

                    if (shed.getMalesNumber() > 0) {
                        technicalSheet.addMaleTechnicalSheetLineListItem(line);
                    }

                    if (shed.getFemalesNumber() > 0) {
                        technicalSheet.addFemaleTechnicalSheetLineListItem(line);
                    }
                }
            }

//            // male technical sheet
//            if(technicalSheet.getMaleTechnicalSheetLineList().isEmpty()) {
//                for (BreedingShed x : technicalSheet.getCenter().getBreedingShedList()) {
//                    if (x.getCenter() == technicalSheet.getCenter() && x.getStatus() == BreedingShedRepository.STATUS_BUSY && x.getMalesNumber() > 0) {
//                        TechnicalSheetLine rline = new TechnicalSheetLine();
//                        rline.setBreedingShed(x);
//                        technicalSheet.addMaleTechnicalSheetLineListItem(rline);
//                    }
//                }
//            }
            // female technical sheet
//            if(technicalSheet.getFemaleTechnicalSheetLineList().isEmpty()) {
//                for (BreedingShed x : technicalSheet.getCenter().getBreedingShedList()) {
//                    if (x.getCenter() == technicalSheet.getCenter() && x.getStatus() == BreedingShedRepository.STATUS_BUSY && x.getFemalesNumber() > 0) {
//                        TechnicalSheetLine rline = new TechnicalSheetLine();
//                        rline.setBreedingShed(x);
//                        technicalSheet.addFemaleTechnicalSheetLineListItem(rline);
//                    }
//                }
//            }
        }

        response.setValues(technicalSheet);
    }

    public void processAllTechnicalSheetLine(ActionRequest request, ActionResponse response)
            throws AxelorException {

        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);
        if (technicalSheet.getMaleTechnicalSheetLineList() != null) {
            for (TechnicalSheetLine line : technicalSheet.getMaleTechnicalSheetLineList()) {
                if (line != null) {
                    processTechnicalSheetLine(request, line);
                }
            }
        }
        if (!technicalSheet.getFemaleTechnicalSheetLineList().isEmpty()) {
            for (TechnicalSheetLine line : technicalSheet.getFemaleTechnicalSheetLineList()) {
                if (line != null) {
                    processTechnicalSheetLine(request, line);
                }
            }
        }
    }

    private void processTechnicalSheetLine(ActionRequest request,TechnicalSheetLine line) {
        try {
            getTechnicalSheetElevTransferLineList(line);
            getElevReformList(line);
            getElevMovementList(line);
            getElevMovementLineList(line);
            getPharmacyTreatmentsLineList(request, line);
        } catch (AxelorException e) {
            throw new RuntimeException(e);
        }

    }

    public void getDateMaleTechnicalSheetLine(ActionRequest request, ActionResponse response)
            throws AxelorException {

        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);
        if (technicalSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if(technicalSheet.getCenter() != null ) {
            Optional<User> usr = technicalSheet.getCenter()
                    .getResponsible().stream().filter(t-> !t.getBlocked()
                            && ( t.getActivateOn() == null || technicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
                            && ( t.getExpiresOn() == null || technicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
                    ).findFirst();

            usr.ifPresent(technicalSheet::setCurrentResponsible);
        }

        List<TechnicalSheet> allLines = technicalSheetRepository.all()
                .filter("self.trackingNumber = ?1 AND self.breed = ?2 AND self.center = ?3 AND self.status != ?4",
                        technicalSheet.getTrackingNumber(), technicalSheet.getBreed(), technicalSheet.getCenter(), TechnicalSheetLineRepository.STATUS_CANCELED)
                .fetch();

        TechnicalSheet stsParent = null;

        Comparator<TechnicalSheet> byDate = Comparator.nullsLast(Comparator.comparing(TechnicalSheet::getDateTime));

        for (TechnicalSheet line : allLines) {
            if (stsParent == null || byDate.compare(line, stsParent) > 0) {
                stsParent = line;
            }
        }

        if(stsParent != null) {
            if(stsParent.getStatus() != TechnicalSheetRepository.STATUS_FINALIZED_SHEET)
            {
                throw new AxelorException(
                        technicalSheet,
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
                        technicalSheet.getReference());
            }

            List<TechnicalSheetLine> allTechnicalSheetLines = technicalSheetLineRepository.all()
                    .filter("self.technicalSheet = ?1 OR self.technicalSheetM = ?1", stsParent)
                    .fetch();

            technicalSheet.setPreviousTechnicalSheet(stsParent);
            technicalSheet.setDailyAge(stsParent.getDailyAge() + 1);
            technicalSheet.setDateTime(stsParent.getDateTime().plusDays(1));
            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
            technicalSheet.setBreed(technicalSheet.getBreed());
            if (!stsParent.getMaleTechnicalSheetLineList().isEmpty()) {
                stsParent.getMaleTechnicalSheetLineList().forEach(
                    line -> {
                        if (line != null) {
                            line.setMalesNumber(line.getMaleRemainingNumber());
                        }
                    });

            }
            for (TechnicalSheetLine line : technicalSheet.getMaleTechnicalSheetLineList()) {
                TechnicalSheetLine stsLineParent = null;

                for (TechnicalSheetLine technicalSheetLine : allTechnicalSheetLines) {
                    if ((technicalSheetLine.getTechnicalSheetM() != null && technicalSheetLine.getTechnicalSheetM().equals(stsParent))
                            && (technicalSheetLine.getBreedingShed() != null && line.getBreedingShed() != null && technicalSheetLine.getBreedingShed().equals(line.getBreedingShed()))
                            && line.getStatus() != TechnicalSheetLineRepository.STATUS_CANCELED) {
                        if (stsLineParent == null) {
                            stsLineParent = technicalSheetLine;
                        }
                    }
                }

                if (stsLineParent != null) {
                    line.setMaleCoefficientVariation(stsLineParent.getMaleCoefficientVariation());
                    line.setMaleHomogeneity(stsLineParent.getMaleHomogeneity());
                    line.setMaleWeight(stsLineParent.getMaleWeight());
                    line.setMaleIsSkipDay(stsLineParent.getMaleIsSkipDay());
                    line.setMaleSkipDayReason(stsLineParent.getMaleSkipDayReason());
                    line.setMaleConsumedProduct(stsLineParent.getMaleConsumedProduct());
                    line.setMaleConsumedProductUnit(stsLineParent.getMaleConsumedProductUnit());
                    line.setMaleConsumedProductQty(stsLineParent.getMaleConsumedProductQty());
                    line.setLightingDuration(stsLineParent.getLightingDuration());
                    line.setIntensity(stsLineParent.getIntensity());
                    line.setTemperatureMin(stsLineParent.getTemperatureMin());
                    line.setTemperatureMax(stsLineParent.getTemperatureMax());
                    line.setTemperatureConsg(stsLineParent.getTemperatureConsg());
                    line.setHumidityMin(stsLineParent.getHumidityMin());
                    line.setHumidityMax(stsLineParent.getHumidityMax());
                    line.setWaterConsumption(stsLineParent.getWaterConsumption());
                    line.setMedicalTreatment(stsLineParent.getMedicalTreatment());
                }
            }
        }
        else {
            technicalSheet.setDailyAge(technicalSheet.getDailyAge());
            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
            technicalSheet.setBreed(technicalSheet.getBreed());
            if (technicalSheet.getMaleTechnicalSheetLineList() != null) {
                technicalSheet.getMaleTechnicalSheetLineList().forEach(
                    line -> {
                        if (line != null) {
                           line.setMalesNumber(line.getBreedingShed().getMalesNumber());
                           if(line.getBreedingShed().getFillingDate() != null) {
                               technicalSheet.setDateTime(line.getBreedingShed().getFillingDate().toLocalDate());
                           }
                           else {
                               technicalSheet.setDateTime(LocalDate.now());
                           }

                        }
                    });
            }

        }

        response.setValues(technicalSheet);
    }

    public void getDateFemaleTechnicalSheetLine(ActionRequest request, ActionResponse response)
            throws AxelorException {

        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);
        if (technicalSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        if(technicalSheet.getCenter() != null) {
            Optional<User> usr = technicalSheet.getCenter()
                    .getResponsible().stream().filter(t-> !t.getBlocked()
                            && ( t.getActivateOn() == null || technicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
                            && ( t.getExpiresOn() == null || technicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
                    ).findFirst();

            usr.ifPresent(technicalSheet::setCurrentResponsible);
        }

        List<TechnicalSheet> allLines = technicalSheetRepository.all()
                .filter("self.trackingNumber = ?1 AND self.breed = ?2 AND self.center = ?3 AND self.status != ?4",
                        technicalSheet.getTrackingNumber(), technicalSheet.getBreed(), technicalSheet.getCenter(), TechnicalSheetLineRepository.STATUS_CANCELED)
                .fetch();

        TechnicalSheet stsParent = null;

        Comparator<TechnicalSheet> byDate = Comparator.nullsLast(Comparator.comparing(TechnicalSheet::getDateTime));

        for (TechnicalSheet line : allLines) {
            if (stsParent == null || byDate.compare(line, stsParent) > 0) {
                stsParent = line;
            }
        }

        if(stsParent != null){
            if(stsParent.getStatus() != TechnicalSheetRepository.STATUS_FINALIZED_SHEET)
            {
                throw new AxelorException(
                        technicalSheet,
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
                        technicalSheet.getReference());
            }

            List<TechnicalSheetLine> allTechnicalSheetLines = technicalSheetLineRepository.all()
                    .filter("self.technicalSheet = ?1 OR self.technicalSheetM = ?1", stsParent)
                    .fetch();

            technicalSheet.setPreviousTechnicalSheet(stsParent);
            technicalSheet.setDailyAge(stsParent.getDailyAge() + 1);
            technicalSheet.setDateTime(stsParent.getDateTime().plusDays(1));
            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
            technicalSheet.setBreed(technicalSheet.getBreed());
            if (!stsParent.getFemaleTechnicalSheetLineList().isEmpty()) {
                stsParent.getFemaleTechnicalSheetLineList().forEach(
                    line -> {
                        if (line != null) {
                            line.setFemalesNumber(line.getFemaleRemainingNumber());
                        }
                    });
            }
            for (TechnicalSheetLine line : technicalSheet.getFemaleTechnicalSheetLineList()) {
                TechnicalSheetLine stsLineParent = null;

                for (TechnicalSheetLine technicalSheetLine : allTechnicalSheetLines) {
                    if ((technicalSheetLine.getTechnicalSheet() != null && technicalSheetLine.getTechnicalSheet().equals(stsParent))
                            && technicalSheetLine.getBreedingShed().equals(line.getBreedingShed())
                            && line.getStatus() != TechnicalSheetLineRepository.STATUS_CANCELED) {
                        if (stsLineParent == null) {
                            stsLineParent = technicalSheetLine;
                        }
                    }
                }

                if (stsLineParent != null) {
                    line.setFemaleCoefficientVariation(stsLineParent.getFemaleCoefficientVariation());
                    line.setFemaleHomogeneity(stsLineParent.getFemaleHomogeneity());
                    line.setFemaleWeight(stsLineParent.getFemaleWeight());
                    line.setFemaleIsSkipDay(stsLineParent.getFemaleIsSkipDay());
                    line.setFemaleSkipDayReason(stsLineParent.getFemaleSkipDayReason());
                    line.setFemaleConsumedProduct(stsLineParent.getFemaleConsumedProduct());
                    line.setFemaleConsumedProductUnit(stsLineParent.getFemaleConsumedProductUnit());
                    line.setFemaleConsumedProductQty(stsLineParent.getFemaleConsumedProductQty());
                    line.setLightingDuration(stsLineParent.getLightingDuration());
                    line.setIntensity(stsLineParent.getIntensity());
                    line.setTemperatureMin(stsLineParent.getTemperatureMin());
                    line.setTemperatureMax(stsLineParent.getTemperatureMax());
                    line.setTemperatureConsg(stsLineParent.getTemperatureConsg());
                    line.setHumidityMin(stsLineParent.getHumidityMin());
                    line.setHumidityMax(stsLineParent.getHumidityMax());
                    line.setWaterConsumption(stsLineParent.getWaterConsumption());
                    line.setMedicalTreatment(stsLineParent.getMedicalTreatment());
                }
            }
        }
        else {
            technicalSheet.setDailyAge(technicalSheet.getDailyAge());
            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
            technicalSheet.setBreed(technicalSheet.getBreed());
            if (!technicalSheet.getFemaleTechnicalSheetLineList().isEmpty()) {
                technicalSheet.getFemaleTechnicalSheetLineList().forEach(
                    line -> {
                        if (line != null) {
                            line.setFemalesNumber(line.getBreedingShed().getFemalesNumber());
                            if(line.getBreedingShed().getFillingDate() != null) {
                                technicalSheet.setDateTime(line.getBreedingShed().getFillingDate().toLocalDate());
                            }
                            else {
                                technicalSheet.setDateTime(LocalDate.now());
                            }

                        }
                    });
            }
        }

        response.setValues(technicalSheet);
    }

//    public void getDateTechnicalSheetLine(ActionRequest request, ActionResponse response)
//            throws AxelorException {
//
//        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);
//        if (technicalSheet == null) {
//            throw new AxelorException(
//                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
//        }
//
//        Optional<User> usr = technicalSheet.getCenter()
//                .getResponsible().stream().filter(t-> !t.getBlocked()
//                        && ( t.getActivateOn() == null || technicalSheet.getDateTime().isAfter(t.getActivateOn().toLocalDate()))
//                        && ( t.getExpiresOn() == null || technicalSheet.getDateTime().isBefore(t.getExpiresOn().toLocalDate()))
//                ).findFirst();
//
//        usr.ifPresent(technicalSheet::setCurrentResponsible);
//
//        List<TechnicalSheet> allLines = technicalSheetRepository.all().fetch();
//        List<TechnicalSheetLine> allTechnicalSheetLines = technicalSheetLineRepository.all().fetch();
//
//        TechnicalSheet stsParent = null;
//
//        Comparator<TechnicalSheet> byDate = Comparator.nullsLast(Comparator.comparing(TechnicalSheet::getDateTime));
//
//        for (TechnicalSheet line : allLines) {
//            if (line.getTrackingNumber().equals(technicalSheet.getTrackingNumber())
//                    && line.getBreed().equals(technicalSheet.getBreed())
//                    && line.getCenter().equals(technicalSheet.getCenter())
//                    && line.getStatus() != TechnicalSheetLineRepository.STATUS_CANCELED) {
//                if (stsParent == null || byDate.compare(line, stsParent) > 0) {
//                    stsParent = line;
//                }
//            }
//        }
//
//        if(stsParent != null){
//            if(stsParent.getStatus() != TechnicalSheetRepository.STATUS_FINALIZED_SHEET)
//            {
//                throw new AxelorException(
//                        technicalSheet,
//                        TraceBackRepository.CATEGORY_MISSING_FIELD,
//                        I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_VALIDATED),
//                        technicalSheet.getReference());
//            }
//
//            technicalSheet.setPreviousTechnicalSheet(stsParent);
//            technicalSheet.setDailyAge(stsParent.getDailyAge() + 1);
//            technicalSheet.setDateTime(stsParent.getDateTime().plusDays(1));
//            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
//            technicalSheet.setBreed(technicalSheet.getBreed());
//            if (!technicalSheet.getTechnicalSheetLineList().isEmpty()) {
//                technicalSheet.getTechnicalSheetLineList().forEach(
//                        line -> {
//                            if (line != null) {
//                                line.setMalesNumber(line.getBreedingShed().getMalesNumber());
//                                line.setFemalesNumber(line.getBreedingShed().getFemalesNumber());
//                            }
//                        });
//            }
//            for (TechnicalSheetLine line : technicalSheet.getTechnicalSheetLineList()) {
//                TechnicalSheetLine stsLineParent = null;
//
//                for (TechnicalSheetLine technicalSheetLine : allTechnicalSheetLines) {
//                    if (technicalSheetLine.getTechnicalSheet().equals(stsParent)
//                            && technicalSheetLine.getBreedingShed().equals(line.getBreedingShed())
//                            && line.getStatus() != TechnicalSheetLineRepository.STATUS_CANCELED) {
//                        if (stsLineParent == null) {
//                            stsLineParent = technicalSheetLine;
//                        }
//                    }
//                }
//
//                if (stsLineParent != null) {
//                    line.setFemaleCoefficientVariation(stsLineParent.getFemaleCoefficientVariation());
//                    line.setMaleCoefficientVariation(stsLineParent.getMaleCoefficientVariation());
//                    line.setFemaleHomogeneity(stsLineParent.getFemaleHomogeneity());
//                    line.setMaleHomogeneity(stsLineParent.getMaleHomogeneity());
//                    line.setFemaleWeight(stsLineParent.getFemaleWeight());
//                    line.setMaleWeight(stsLineParent.getMaleWeight());
//                    line.setFemaleConsumedProduct(stsLineParent.getFemaleConsumedProduct());
//                    line.setMaleConsumedProduct(stsLineParent.getMaleConsumedProduct());
//                    line.setFemaleConsumedProductUnit(stsLineParent.getFemaleConsumedProductUnit());
//                    line.setMaleConsumedProductUnit(stsLineParent.getMaleConsumedProductUnit());
//                    line.setFemaleConsumedProductQty(stsLineParent.getFemaleConsumedProductQty());
//                    line.setLightingDuration(stsLineParent.getLightingDuration());
//                    line.setIntensity(stsLineParent.getIntensity());
//                    line.setTemperatureMin(stsLineParent.getTemperatureMin());
//                    line.setTemperatureMax(stsLineParent.getTemperatureMax());
//                    line.setTemperatureConsg(stsLineParent.getTemperatureConsg());
//                    line.setHumidityMin(stsLineParent.getHumidityMin());
//                    line.setHumidityMax(stsLineParent.getHumidityMax());
//                    line.setWaterConsumption(stsLineParent.getWaterConsumption());
//                }
//            }
//        }
//        else {
//            technicalSheet.setDailyAge(technicalSheet.getDailyAge());
//            technicalSheet.setTrackingNumber(technicalSheet.getTrackingNumber());
//            technicalSheet.setBreed(technicalSheet.getBreed());
//            if (!technicalSheet.getTechnicalSheetLineList().isEmpty()) {
//                technicalSheet.getTechnicalSheetLineList().forEach(
//                    line -> {
//                        if (line != null) {
//                           line.setMalesNumber(line.getBreedingShed().getMalesNumber());
//                           line.setFemalesNumber(line.getBreedingShed().getFemalesNumber());
//                           if(line.getBreedingShed().getFillingDate() != null) {
//                               technicalSheet.setDateTime(line.getBreedingShed().getFillingDate().toLocalDate());
//                           }
//                           else {
//                               technicalSheet.setDateTime(LocalDate.now());
//                           }
//
//                        }
//                    });
//            }
//
//        }
//
//        response.setValues(technicalSheet);
//    }

    public void technicalSheetLineValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        TechnicalSheetLine technicalSheet = request.getContext().asType(TechnicalSheetLine.class);
        Long id = technicalSheet.getId();

        if (technicalSheet != null ) {
            technicalSheetService.ValidateLine(technicalSheet);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }
        response.setValues(technicalSheet);
        response.setReload(true);
    }

    public TechnicalSheetLine getTechnicalSheetElevTransferLineList(TechnicalSheetLine technicalSheet)
            throws AxelorException {

        if (technicalSheet != null) {
            technicalSheet.clearElevTransferLineList();

            for (ElevTransferLine x : elevTransferLineRepository.all().filter("self.technicalSheetLine IS NULL AND self.breedingShed = ?1 AND self.elevTransfer.status = ?2", technicalSheet.getBreedingShed(), ElevTransferRepository.STATUS_RECEIVED).fetch()) {
                if ((( technicalSheet.getTechnicalSheet() != null && x.getElevTransfer().getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber()) || (technicalSheet.getTechnicalSheetM() != null && x.getElevTransfer().getTrackingNumber() == technicalSheet.getTechnicalSheetM().getTrackingNumber())))
                {
                    x.setTechnicalSheetLine(technicalSheet);
                    technicalSheet.addElevTransferLineListItem(x);
                    technicalSheet.setTransfer(x.getToTransferChickensNumber());
                }
            }
        }
       return  technicalSheet;
    }

    public TechnicalSheetLine getElevMovementList(TechnicalSheetLine technicalSheet)
            throws AxelorException {

        if (technicalSheet != null) {
            technicalSheet.clearElevMovementList();
            int totalMovedNumber = 0;

            for (StartElevMovement x : startElevMovementRepository.all().filter("self.technicalSheetLine IS NULL AND self.status = ?1 AND self.breedingShed = ?2", StartElevMovementRepository.STATUS_EXECUTING, technicalSheet.getBreedingShed()).fetch()) {
                if (((technicalSheet.getTechnicalSheet()!= null && x.getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber()) || (technicalSheet.getTechnicalSheetM() != null && x.getTrackingNumber() == technicalSheet.getTechnicalSheetM().getTrackingNumber())))
                {
                    x.setTechnicalSheetLine(technicalSheet);
                    technicalSheet.addElevMovementListItem(x);
                    totalMovedNumber += x.getStartElevMovementLineList().stream()
                            .mapToInt(line -> line.getMovedNumber())
                            .sum();
                }
            }
            technicalSheet.setDisplacementOut(totalMovedNumber);
        }
         return technicalSheet;
    }
    public TechnicalSheetLine getElevMovementLineList(TechnicalSheetLine technicalSheet)
            throws AxelorException {

        if (technicalSheet != null) {
            technicalSheet.clearElevMovementLineList();
            for (StartElevMovementLine x : startElevMovementLineRepository.all().filter("self.technicalSheetLine IS NULL AND self.breedingShed = ?1 AND self.elevMovement.status = ?2", technicalSheet.getBreedingShed(), StartElevMovementRepository.STATUS_EXECUTING).fetch()) {
                if (((technicalSheet.getTechnicalSheet() != null && x.getElevMovement().getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber()) || (technicalSheet.getTechnicalSheetM() != null && x.getElevMovement().getTrackingNumber() == technicalSheet.getTechnicalSheetM().getTrackingNumber())))
                {
                    x.setTechnicalSheetLine(technicalSheet);
                    technicalSheet.addElevMovementLineListItem(x);
                    technicalSheet.setDisplacementIn(x.getMovedNumber());
                }
            }

        }
        return  technicalSheet;
    }

    public TechnicalSheetLine getPharmacyTreatmentsLineList(ActionRequest request,TechnicalSheetLine technicalSheet)
            throws AxelorException {

        LocalDate dateTime = (LocalDate) request.getContext().get("dateTime");
        if (technicalSheet != null) {
            technicalSheet.clearPharmacyTreatmentLineList();

            for (PharmacyTreatmentLine x : pharmacyTreatmentLineRepo.all().filter("self.technicalSheetLine IS NULL AND self.pharmacyTreatment.status = ?1 AND self.pharmacyTreatment.breedingShed = ?2", PharmacyTreatmentRepository.STATUS_EXECUTING, technicalSheet.getBreedingShed()).fetch()) {
                if (((technicalSheet.getTechnicalSheet() != null && x.getPharmacyTreatment().getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber()) || (technicalSheet.getTechnicalSheetM() != null && x.getPharmacyTreatment().getTrackingNumber() == technicalSheet.getTechnicalSheetM().getTrackingNumber()))
                        && (x.getPharmacyTreatment().getFromRealDate() != null && ( dateTime.isEqual(x.getPharmacyTreatment().getFromRealDate()) || dateTime.isAfter(x.getPharmacyTreatment().getFromRealDate()))
                        && ( x.getPharmacyTreatment().getToRealDate() != null && (dateTime.isEqual(x.getPharmacyTreatment().getToRealDate()) || dateTime.isBefore(x.getPharmacyTreatment().getToRealDate())))
                    ))
                {
                    x.setTechnicalSheetLine(technicalSheet);
                    technicalSheet.setMedicalTreatment(x.getMedicalTreatment());
                    technicalSheet.addPharmacyTreatmentLineListItem(x);
                }
            }

        }
        return technicalSheet;
    }

    public TechnicalSheetLine getElevReformList(TechnicalSheetLine technicalSheet)
            throws AxelorException {
        //TechnicalSheetLine technicalSheet = request.getContext().asType(TechnicalSheetLine.class);

        if (technicalSheet != null) {
            technicalSheet.clearElevReformList();

            for (ElevReform x : elevReformRepository.all().filter("self.technicalSheetLine IS NULL AND self.status = ?1 AND self.breedingShed = ?2", StartElevReformRepository.STATUS_EXECUTING, technicalSheet.getBreedingShed()).fetch()) {
                if (((technicalSheet.getTechnicalSheetM() != null && x.getTrackingNumber() == technicalSheet.getTechnicalSheetM().getTrackingNumber()) || (technicalSheet.getTechnicalSheet() != null && x.getTrackingNumber() == technicalSheet.getTechnicalSheet().getTrackingNumber())))
                {
                    if(technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0 && technicalSheet.getBreedingShed().getReceivedFemalesNumber() == 0){
                        technicalSheet.setMaleReform(x.getMaleReformNumber());
                    }
                    if(technicalSheet.getBreedingShed().getReceivedMalesNumber() == 0 && technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0){
                        technicalSheet.setFemaleReform(x.getFemaleReformNumber());
                    }
                    if(technicalSheet.getBreedingShed().getReceivedMalesNumber() > 0 && technicalSheet.getBreedingShed().getReceivedFemalesNumber() > 0){
                        technicalSheet.setMaleReform(x.getMaleReformNumber());
                        technicalSheet.setFemaleReform(x.getFemaleReformNumber());
                    }

                    x.setTechnicalSheetLine(technicalSheet);
                    technicalSheet.addElevReformListItem(x);
                }
            }
        }
        return technicalSheet;
    }

    @Transactional
    public void technicalSheetValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        TechnicalSheet technicalSheet = request.getContext().asType(TechnicalSheet.class);
        Long id = technicalSheet.getId();

        if (technicalSheet != null ) {
            technicalSheetService.Validate(technicalSheet);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }

        response.setValues(technicalSheet);
        response.setReload(true);
    }

}
