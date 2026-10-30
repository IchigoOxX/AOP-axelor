package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.LivestockFeedProduction;

public interface LivestockFeedProductionService {
    public LivestockFeedProduction Validate(LivestockFeedProduction livestockFeedProduction) throws AxelorException;
    public LivestockFeedProduction Cancel(LivestockFeedProduction livestockFeedProduction) throws AxelorException;
}
