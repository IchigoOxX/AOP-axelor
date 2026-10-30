package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.Contract;

public interface ContractService {
    public Contract SetVehicleOccupied(Contract contract) throws AxelorException;
    public Contract SetVehicleFree(Contract contract) throws AxelorException;
}
