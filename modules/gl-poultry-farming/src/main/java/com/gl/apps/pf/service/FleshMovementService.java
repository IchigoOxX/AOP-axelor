package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.FleshMovement;

public interface FleshMovementService {
    public FleshMovement Validate(FleshMovement fleshMovement) throws AxelorException;
    public FleshMovement Cancel(FleshMovement fleshMovement) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(FleshMovement fleshMovement) throws AxelorException;
}
