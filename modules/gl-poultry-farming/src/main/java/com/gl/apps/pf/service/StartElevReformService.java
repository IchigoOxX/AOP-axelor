package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.Slaughterhouse;
import com.gl.apps.pf.db.StartElevReform;

public interface StartElevReformService {
    public StartElevReform Validate(StartElevReform startElevReform) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(StartElevReform startElevReform)
            throws AxelorException;
}
