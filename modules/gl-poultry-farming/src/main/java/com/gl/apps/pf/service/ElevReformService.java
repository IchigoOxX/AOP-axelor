package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevReform;

public interface ElevReformService {
    public ElevReform Validate(ElevReform breedingReform) throws AxelorException;
    public ElevReform Cancel(ElevReform breedingReform) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ElevReform breedingReform)
            throws AxelorException;
}
