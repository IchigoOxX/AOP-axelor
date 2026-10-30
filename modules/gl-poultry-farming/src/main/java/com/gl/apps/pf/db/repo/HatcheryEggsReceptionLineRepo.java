package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.HatcheryEggsReceptionLine;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionLineRepository;
import com.google.inject.Inject;

public class HatcheryEggsReceptionLineRepo extends HatcheryEggsReceptionLineRepository {

  @Inject private SequenceService sequenceService;

  @Override
  public HatcheryEggsReceptionLine save(HatcheryEggsReceptionLine entity) {
    if (entity.getLineNumber() == null || Integer.parseInt(entity.getLineNumber()) == 0) {

      Integer ln =
          entity.getEggsReception().getHatcheryEggsReceptionLineList().stream()
              .map(n -> Integer.parseInt(n.getLineNumber() != null ? n.getLineNumber() : "0"))
              .max(Integer::compareTo)
              .orElse(0);
      ln++;
      String seq = String.format("%03d", ln);
      entity.setLineNumber(seq);
    }

    return super.save(entity);
  }

  @Override
  public void remove(HatcheryEggsReceptionLine entity) {
    super.remove(entity);
  }
}
