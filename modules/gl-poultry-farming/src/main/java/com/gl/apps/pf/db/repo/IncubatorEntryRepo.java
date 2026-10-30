package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.Breed;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

public class IncubatorEntryRepo extends IncubatorEntryRepository{
    @Inject
    private SequenceService sequenceService;

    @Override
    public IncubatorEntry save(IncubatorEntry entity) {
        if (entity.getReference() == null) {
            String seq = null;
            try {
                seq = sequenceService.getSequenceNumber(Ipoultryfarming.INCUBATOR_ENTRY_SEQ, IncubatorEntry.class, "reference", null);

            } catch (AxelorException e) {
                TraceBackService.traceExceptionFromSaveMethod(e);
            }
            if (seq == null) {
                try {
                    throw new AxelorException(
                            entity,
                            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                            I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_FOUND),
                            I18n.get(BaseExceptionMessage.EXCEPTION));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }
            }
            entity.setReference(seq);
        }

        if (entity.getStatus() == IncubatorEntryRepository.STATUS_NOT_EXECUTED) {
            if (entity.getIncubatorEntryLines() != null) {
                entity.getIncubatorEntryLines().forEach(
                    line -> {
                        if (line != null) {
                            if(line.getIncubatorTrolley() != null) {
                                line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_RESERVED);
                                line.setStatus(IncubatorTrolleyRepository.STATUS_RESERVED);
                            }
                        }
                    });
            }
        }

        return super.save(entity);
    }

}

