package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.ReproProductionLine;
import com.gl.apps.pf.db.repo.ReproProductionLineRepository;
import com.google.inject.Inject;

public class ReproProductionLineRepo extends ReproProductionLineRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public ReproProductionLine save(ReproProductionLine entity) {
    if (entity.getLineNumber() == null || entity.getLineNumber() == 0) {

      Integer ln =
          entity.getReproProduction().getReproProductionLineList().stream()
              .map(n -> n.getLineNumber() != null ? n.getLineNumber() : 0)
              .max(Integer::compareTo)
              .orElse(0);
      ln++;
      entity.setLineNumber(ln);
    }

    return super.save(entity);
  }

  @Override
  public void remove(ReproProductionLine entity) {
    super.remove(entity);
  }
}
