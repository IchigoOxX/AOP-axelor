package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorExit;

public interface IncubatorExitService {
    public IncubatorExit Validate(IncubatorExit incubatorExit) throws AxelorException;
    public IncubatorExit Cancel(IncubatorExit incubatorExit) throws AxelorException;
    public IncubatorExit Revalidate(IncubatorExit incubatorExit) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(IncubatorExit incubatorExit) throws AxelorException;
}
