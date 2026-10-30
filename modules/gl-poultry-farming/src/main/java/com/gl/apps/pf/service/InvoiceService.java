package com.gl.apps.pf.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;

public interface InvoiceService {
    public Invoice SetContractPaid(Invoice invoice) throws AxelorException;
}
