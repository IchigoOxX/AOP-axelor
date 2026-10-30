package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.ProductOrder;

public interface IncubatorEntryService {
    public IncubatorEntry Validate(IncubatorEntry incubatorEntry) throws AxelorException;
    public IncubatorEntry Cancel(IncubatorEntry incubatorEntry) throws AxelorException;
    public IncubatorEntry Revalidate(IncubatorEntry incubatorEntry) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(IncubatorEntry incubatorEntry) throws AxelorException;
}
