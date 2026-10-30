package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.FleshChicksReception;

public interface FleshChicksReceptionService {
    public FleshChicksReception Validate(FleshChicksReception fleshChicksReception) throws AxelorException;
    public FleshChicksReception Cancel(FleshChicksReception fleshChicksReception) throws AxelorException;
    //public StockMove CreateOrUpdateStockMove(FleshChicksReception fleshChicksReception) throws AxelorException;
}
