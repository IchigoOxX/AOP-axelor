package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ReproElevMovement;
import com.gl.apps.pf.db.StartElevMovement;

public interface ReproElevMovementService {
    public ReproElevMovement Validate(ReproElevMovement reproElevMovement) throws AxelorException;
    public ReproElevMovement Cancel(ReproElevMovement reproElevMovement) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ReproElevMovement reproElevMovement)
            throws AxelorException;
}
