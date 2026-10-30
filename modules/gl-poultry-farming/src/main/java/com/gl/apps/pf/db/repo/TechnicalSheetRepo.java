package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import jakarta.persistence.EntityManager;

public class TechnicalSheetRepo extends  TechnicalSheetRepository{
    @Inject
    private SequenceService sequenceService;
    @Transactional
    @Override
    public TechnicalSheet save(TechnicalSheet entity) {
        //EntityManager entityManager = JPA.em();
        if (entity.getReference() == null) {
            String seq = null;
            try {
                seq = sequenceService.getSequenceNumber(Ipoultryfarming.TECHNICAL_SHEET_SEQ, IncubatorsLoadingProgram.class, "reference", null);
            } catch (AxelorException e) {
                TraceBackService.traceExceptionFromSaveMethod(e);
            }
            if (seq == null) {
                try {
                    throw new AxelorException(
                            entity,
                            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                            I18n.get(PoultryFarmingExceptionMessage.START_TECHNICAL_SHEET_SEQUENCE_NOT_FOUND),
                            I18n.get(BaseExceptionMessage.EXCEPTION));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }
            }
            entity.setReference(seq);
        }
//        for (TechnicalSheetLine line : entity.getFemaleTechnicalSheetLineList()) {
//            line.setTechnicalSheet(entity);
//            if (line.getId() != null) {
//                TechnicalSheetLine existingLine = entityManager.find(TechnicalSheetLine.class, line.getId());
//                if (existingLine == null) {
//                    try {
//                        throw new AxelorException(
//                                TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                I18n.get("TechnicalSheetLine with ID " + line.getId() + " not found"));
//                    } catch (AxelorException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }
//
//        for (TechnicalSheetLine line : entity.getMaleTechnicalSheetLineList()) {
//            line.setTechnicalSheetM(entity);
//            if (line.getId() != null) {
//                TechnicalSheetLine existingLine = entityManager.find(TechnicalSheetLine.class, line.getId());
//                if (existingLine == null) {
//                    try {
//                        throw new AxelorException(
//                                TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                I18n.get("TechnicalSheetLine with ID " + line.getId() + " not found"));
//                    } catch (AxelorException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        }
//
//        if (entity.getId() == null) {
//            super.persist(entity);
//        } else {
//            super.merge(entity);
//        }
        return super.save(entity);
    }
}


