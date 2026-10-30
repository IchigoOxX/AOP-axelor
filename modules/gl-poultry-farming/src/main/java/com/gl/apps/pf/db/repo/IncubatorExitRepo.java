package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorExit;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

public class IncubatorExitRepo extends IncubatorExitRepository {
    @Inject
    private SequenceService sequenceService;

    @Override
    public IncubatorExit save(IncubatorExit entity) {
        if (entity.getReference() == null) {
            String seq = null;
            try {
                seq = sequenceService.getSequenceNumber(Ipoultryfarming.INCUBATOR_EXIT_SEQ, IncubatorEntry.class, "reference", null);

            } catch (AxelorException e) {
                TraceBackService.traceExceptionFromSaveMethod(e);
            }
            if (seq == null) {
                try {
                    throw new AxelorException(
                            entity,
                            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                            I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND),
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

