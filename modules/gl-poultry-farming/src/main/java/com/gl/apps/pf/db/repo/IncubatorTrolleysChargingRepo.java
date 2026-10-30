package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.IncubatorTrolleysCharging;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class IncubatorTrolleysChargingRepo extends IncubatorTrolleysChargingRepository {

  @Inject private SequenceService sequenceService;

  @Inject private IncubatorTrolleysChargingLineRepo trolleysChargingLineRepo;

  @Override
  public IncubatorTrolleysCharging save(IncubatorTrolleysCharging entity) {
    if (entity.getReference() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_INCUBATORSTROLLEYSCHARGINGSEQ, IncubatorsLoadingProgram.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATORTROLLEYSCHARGING_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }
      entity.setReference(seq);
    }
    if (entity.getIncubatorTrolleysChargingLineList() != null) {
      entity
          .getIncubatorTrolleysChargingLineList()
          .forEach(line -> trolleysChargingLineRepo.save(line));
    }
    return super.save(entity);
  }

  @Override
  public void remove(IncubatorTrolleysCharging entity) {
    super.remove(entity);
  }
}

