package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.IncubatorsLoadingProgramLine;
import com.gl.apps.pf.db.repo.IncubatorsLoadingProgramLineRepository;
import com.google.inject.Inject;

public class IncubatorsLoadingProgramLineRepo extends IncubatorsLoadingProgramLineRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public IncubatorsLoadingProgramLine save(IncubatorsLoadingProgramLine entity) {
    if (entity.getLineNumber() == null || Integer.parseInt(entity.getLineNumber()) == 0) {

      Integer ln =
          entity.getLoadingProgram().getLoadingProgramLineList().stream()
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
  public void remove(IncubatorsLoadingProgramLine entity) {
    super.remove(entity);
  }
}
