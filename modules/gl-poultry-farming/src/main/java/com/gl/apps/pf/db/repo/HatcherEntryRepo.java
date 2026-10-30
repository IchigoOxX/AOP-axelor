package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorExit;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

public class HatcherEntryRepo extends HatcherEntryRepository{
    @Inject
    private SequenceService sequenceService;
    @Override
    public HatcherEntry save(HatcherEntry entity) {
        if (entity.getReference() == null) {
            String seq = null;
            try {
                seq = sequenceService.getSequenceNumber(Ipoultryfarming.HATCHER_ENTRY_SEQ, IncubatorEntry.class, "reference", null);

            } catch (AxelorException e) {
                TraceBackService.traceExceptionFromSaveMethod(e);
            }
            if (seq == null) {
                try {
                    throw new AxelorException(
                            entity,
                            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                            I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_FOUND),
                            I18n.get(BaseExceptionMessage.EXCEPTION));
                } catch (AxelorException e) {
                    throw new RuntimeException(e);
                }
            }
            entity.setReference(seq);
        }
        if (entity.getStatus() == HatcherEntryRepository.STATUS_NOT_EXECUTED) {
            if (entity.getHatcherEntryLinesList() != null ) {
                entity.getHatcherEntryLinesList().forEach(
                        line -> {
                            if (line != null) {
                                if(line.getHatcherTrolley() != null) {
                                    line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_RESERVED);
                                }
                            }
                        });
            }
        }
        return super.save(entity);
    }
}

