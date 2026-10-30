package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.FleshLoadingProgram;

public interface FleshLoadingProgramService {
    public FleshLoadingProgram ActivateBreedingShed(FleshLoadingProgram fleshLoadingProgram) throws AxelorException;
    public FleshLoadingProgram validate(FleshLoadingProgram fleshLoadingProgram) throws AxelorException;
}
