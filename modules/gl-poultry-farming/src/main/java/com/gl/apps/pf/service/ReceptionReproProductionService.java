package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.ReceptionReproProduction;
import com.gl.apps.pf.db.ReproElevMovement;

public interface ReceptionReproProductionService {
    public ReceptionReproProduction Validate(ReceptionReproProduction receptionReproProduction) throws AxelorException;
    public ReceptionReproProduction Cancel(ReceptionReproProduction receptionReproProduction) throws AxelorException;

    public StockMove CreateOrUpdateStockMove(ReceptionReproProduction receptionReproProduction)
            throws AxelorException;
}
