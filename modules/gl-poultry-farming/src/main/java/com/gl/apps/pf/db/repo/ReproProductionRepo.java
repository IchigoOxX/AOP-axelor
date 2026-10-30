package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReproProductionRepo extends ReproProductionRepository {
  @Inject private SequenceService sequenceService;
  @Transactional
  @Override
  public ReproProduction save(ReproProduction entity) {
    if (entity.getReference() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_REPROPRODUCTIONSEQ, IncubatorsLoadingProgram.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.REPROPRODUCTION_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }
      entity.setReference(seq);
    }

    return super.save(entity);
  }

  @Override
  public void remove(ReproProduction entity) {
    super.remove(entity);
  }
}

