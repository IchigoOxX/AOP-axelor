package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.rpc.Context;
import com.gl.apps.pf.db.StartTechnicalSheet;

public interface StockMoveService {
    public StockMove SetValuesLineStockMove(StockMove stockMove) throws AxelorException;
    public StockMove saveTest(Context context) throws AxelorException;
    public StockMove addChicksReception(StockMove stockmov) throws AxelorException;


}
