package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.StartReceivingChicks;

public interface PharmacyTreatmentService {
    public PharmacyTreatment Validate(PharmacyTreatment pharmacyTreatment) throws AxelorException;
    public StockMove CreateOrUpdateStockMove(PharmacyTreatment pharmacyTreatment)
            throws AxelorException;
}
