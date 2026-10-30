package com.gl.apps.pf.db.repo;

import static com.gl.apps.pf.db.repo.IncubatorTrolleysChargingRepository.STATUS_NOT_EXECUTED;

import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.IncubatorTrolleysChargingLine;
import com.gl.apps.pf.db.repo.IncubatorTrolleysChargingLineRepository;
import com.google.inject.Inject;

public class IncubatorTrolleysChargingLineRepo extends IncubatorTrolleysChargingLineRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public IncubatorTrolleysChargingLine save(IncubatorTrolleysChargingLine entity) {
    if (entity.getLineNumber() == null || Integer.parseInt(entity.getLineNumber()) == 0) {

      Integer ln =
          entity.getIncubatorTrolleysCharging().getIncubatorTrolleysChargingLineList().stream()
              .map(n -> Integer.parseInt(n.getLineNumber() != null ? n.getLineNumber() : "0"))
              .max(Integer::compareTo)
              .orElse(0);
      ln++;
      String seq = String.format("%03d", ln);
      entity.setLineNumber(seq);
    }

    if (entity.getStatus() == null) {
      entity.setStatus(STATUS_NOT_EXECUTED);
    }

    return super.save(entity);
  }

  @Override
  public void remove(IncubatorTrolleysChargingLine entity) {
    super.remove(entity);
  }
}
