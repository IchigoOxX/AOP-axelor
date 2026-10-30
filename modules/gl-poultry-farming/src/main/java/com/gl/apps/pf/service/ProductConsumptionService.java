package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ProductConsumption;

public interface ProductConsumptionService {
    public ProductConsumption Validate(ProductConsumption productConsumption) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(ProductConsumption productConsumption) throws AxelorException;
    public ProductConsumption Cancel(ProductConsumption productConsumption) throws AxelorException;
}
