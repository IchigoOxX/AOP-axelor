package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.StartTechnicalSheet;

public interface StartTechnicalSheetService {
    public StartTechnicalSheet Validate(StartTechnicalSheet startTechnicalSheet) throws AxelorException;
}
