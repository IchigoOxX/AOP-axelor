package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.Hatchery;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.repo.HatcheryRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class HatcheryRepo extends HatcheryRepository {

  @Inject private SequenceService sequenceService;

  @Inject private IncubatorsLoadingProgramLineRepo loadingProgramLineRepo;

  @Override
  public Hatchery save(Hatchery entity) {
    if (entity.getHatcheryRef() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_HATCHERYSEQ, IncubatorsLoadingProgram.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }
      entity.setHatcheryRef(seq);
    }
    // if (entity.getHatcheryEggsReceptionLineList() != null) {
    // entity.getHatcheryEggsReceptionLineList().forEach(line ->
    // hatcheryEggsReceptionLineRepo.save(line));
    // }
    return super.save(entity);
  }

  @Override
  public void remove(Hatchery entity) {
    super.remove(entity);
  }
}

