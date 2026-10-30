package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.ReproTechnicalSheet;
import com.gl.apps.pf.db.StartTechnicalSheet;

public interface ReproTechnicalSheetService {
    public ReproTechnicalSheet Validate(ReproTechnicalSheet reproTechnicalSheet) throws AxelorException;
}
