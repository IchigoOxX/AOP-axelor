package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.HatcheryDeliverySale;
import com.gl.apps.pf.db.HatcheryEggExit;

public interface HatcheryDeliverySaleService {
    public HatcheryDeliverySale Validate(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException;
    public HatcheryDeliverySale Cancel(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException;
    public HatcheryDeliverySale Revalidate(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException;
}
