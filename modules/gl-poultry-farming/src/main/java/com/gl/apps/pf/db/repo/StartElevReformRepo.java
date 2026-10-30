package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.StartElevReform;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StartElevReformRepo extends StartElevReformRepository{
    @Inject
    private SequenceService sequenceService;
    @Transactional
    @Override
    public StartElevReform save(StartElevReform entity) {
        if (entity.getReference() == null) {
            String seq = null;
            try {
                seq = sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_START_ELEV_REFORM_SEQ, IncubatorsLoadingProgram.class, "reference", null);
            } catch (AxelorException e) {
                TraceBackService.traceExceptionFromSaveMethod(e);
            }
            if (seq == null) {
                try {
                    throw new AxelorException(
                            entity,
                            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                            I18n.get(PoultryFarmingExceptionMessage.START_ELEV_REFORM_SEQUENCE_NOT_FOUND),
                            I18n.get(BaseExceptionMessage.EXCEPTION));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }
            }
            entity.setReference(seq);
        }
        return super.save(entity);
    }
}

