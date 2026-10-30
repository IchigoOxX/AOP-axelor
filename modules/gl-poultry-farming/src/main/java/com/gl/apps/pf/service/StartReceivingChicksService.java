package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.Center;
import com.gl.apps.pf.db.HatcheryEggsReception;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.StartReceivingChicks;

public interface StartReceivingChicksService {
    public StartReceivingChicks Validate(StartReceivingChicks startReceivingChicks) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(StartReceivingChicks startReceivingChicks)
            throws AxelorException;
    public StartReceivingChicks Cancel(StartReceivingChicks startReceivingChicks) throws AxelorException;
//    public StartReceivingChicks SetActivationBreedingShedList(StartReceivingChicks startReceivingChicks, Center center) throws AxelorException;
}
