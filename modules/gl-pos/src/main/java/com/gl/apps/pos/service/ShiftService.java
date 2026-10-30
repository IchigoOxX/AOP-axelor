package com.gl.apps.pos.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.pos.db.Shift;

public interface ShiftService {

    public Shift updateShiftAfterSale(Long shiftId, Long saleOrderId) throws AxelorException;

}
