package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ReproElevReform;

public interface ReproElevReformService {
    public ReproElevReform Validate(ReproElevReform reproElevReform) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ReproElevReform reproElevReform)
            throws AxelorException;
}
