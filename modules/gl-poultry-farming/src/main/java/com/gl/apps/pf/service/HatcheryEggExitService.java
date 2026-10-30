package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.HatcherExit;
import com.gl.apps.pf.db.HatcheryEggExit;

public interface HatcheryEggExitService {
    public HatcheryEggExit Validate(HatcheryEggExit hatcheryEggExit) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(HatcheryEggExit hatcheryEggExit) throws AxelorException;
    public HatcheryEggExit Cancel(HatcheryEggExit hatcheryEggExit) throws AxelorException;
    public HatcheryEggExit Revalidate(HatcheryEggExit hatcheryEggExit) throws AxelorException;
}
