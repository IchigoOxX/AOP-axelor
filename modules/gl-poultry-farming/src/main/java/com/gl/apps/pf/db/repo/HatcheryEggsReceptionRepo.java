package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.HatcheryEggsReception;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.service.HatcheryEggsReceptionServiceImpl;
import com.google.inject.Inject;

public class HatcheryEggsReceptionRepo extends HatcheryEggsReceptionRepository {

  @Inject private SequenceService sequenceService;
  @Inject private HatcheryEggsReceptionServiceImpl eggsReceptionService;
  @Inject private HatcheryEggsReceptionLineRepo hatcheryEggsReceptionLineRepo;

  @Override
  public HatcheryEggsReception save(HatcheryEggsReception entity) {
    if (entity.getReference() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_HATCHERYEGGSRECEPTIONSEQ, IncubatorsLoadingProgram.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERYEGGSRECEPTION_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }
      entity.setReference(seq);
    }
    if (entity.getHatcheryEggsReceptionLineList() != null) {
      entity
          .getHatcheryEggsReceptionLineList()
          .forEach(line -> hatcheryEggsReceptionLineRepo.save(line));
    }
    entity = super.save(entity);
    return entity;
  }

  @Override
  public void remove(HatcheryEggsReception entity) {
    super.remove(entity);
  }
}

