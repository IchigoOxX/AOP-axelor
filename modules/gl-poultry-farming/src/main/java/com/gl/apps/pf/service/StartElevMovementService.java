package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.StartElevMovement;

public interface StartElevMovementService {
    public StartElevMovement Validate(StartElevMovement startElevMovement) throws AxelorException;
    public StartElevMovement Cancel(StartElevMovement startElevMovement) throws AxelorException;
   public StockMove CreateOrUpdateStockMove(StartElevMovement startElevMovement)
           throws AxelorException;
}
