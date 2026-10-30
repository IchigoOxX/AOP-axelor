package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.MaintenanceEnergyConsumption;

public interface MaintenanceEnergyConsumptionService {
    public MaintenanceEnergyConsumption Validate(MaintenanceEnergyConsumption maintenanceEnergyConsumption) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(MaintenanceEnergyConsumption maintenanceEnergyConsumption) throws AxelorException;
}
