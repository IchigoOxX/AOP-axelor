package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.LivestockFeedExternalExit;
import com.gl.apps.pf.db.LivestockFeedProduction;

public interface LivestockFeedExternalExitService{
    public LivestockFeedExternalExit Validate(LivestockFeedExternalExit livestockFeedExternalExit) throws AxelorException;
    public LivestockFeedExternalExit Cancel(LivestockFeedExternalExit livestockFeedExternalExit) throws AxelorException;
}
