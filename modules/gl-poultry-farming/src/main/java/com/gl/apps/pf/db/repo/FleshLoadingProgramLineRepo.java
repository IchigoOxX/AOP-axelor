package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.service.administration.SequenceService;
import com.gl.apps.pf.db.FleshLoadingProgramLine;
import com.gl.apps.pf.db.IncubatorsLoadingProgramLine;
import com.google.inject.Inject;

public class FleshLoadingProgramLineRepo extends FleshLoadingProgramLineRepository{
    @Inject
    private SequenceService sequenceService;

    @Override
    public FleshLoadingProgramLine save(FleshLoadingProgramLine entity) {
        if (entity.getLineNumber() == null || Integer.parseInt(entity.getLineNumber()) == 0) {

            Integer ln =
                    entity.getFleshLoadingProgram().getFleshLoadingProgramLineList().stream()
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
    public void remove(FleshLoadingProgramLine entity) {
        super.remove(entity);
    }

}
