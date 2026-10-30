package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.IncubatorExit;
import com.gl.apps.pf.db.PharmacyTreatment;

public interface MaintenanceMachineService {
    public Machine Validate(Machine machine) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(Machine machine) throws AxelorException;
}
