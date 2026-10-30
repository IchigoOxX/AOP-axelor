package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.FleshLoadingProgramLine;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.HatcherExit;
import com.gl.apps.pf.db.HatcherExitLine;

public interface HatcherExitService {
    public HatcherExit Validate(HatcherExit hatcherExit) throws AxelorException;
    public HatcherExit Cancel(HatcherExit hatcherExit) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(HatcherExit hatcherExit) throws AxelorException;
    public StockMove CreateOrUpdateStockMoveFleshChickRecept(FleshLoadingProgramLine fleshLoadingProgramLine) throws AxelorException;
    public HatcherExit Revalidate(HatcherExit hatcherExit) throws AxelorException;
    public HatcherExit CalculateReceptionValues(HatcherExit hatcherExit) throws AxelorException;
    public HatcherExit redistributeSafeValues(HatcherExitLine changedLine) throws AxelorException;
    public HatcherExit redistributeSafeValuesMortality(HatcherExitLine changedLine) throws AxelorException;
    public HatcherExit redistributeSafeValuesSort(HatcherExitLine changedLine) throws AxelorException;
}
