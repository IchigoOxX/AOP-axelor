package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.StartReceivingChicks;

public interface ElevTransferService {
    public ElevTransfer Validate(ElevTransfer elevTransfer) throws AxelorException;
    public ElevTransfer Cancel(ElevTransfer elevTransfer) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ElevTransfer elevTransfer)
            throws AxelorException;
}
