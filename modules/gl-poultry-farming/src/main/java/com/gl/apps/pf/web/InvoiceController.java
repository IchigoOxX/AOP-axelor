package com.gl.apps.pf.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.InvoiceService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class InvoiceController {
    @Inject private InvoiceService invoiceService;
    @Transactional
    public void SetInvoicePaid(ActionRequest request, ActionResponse response)
        throws AxelorException {
    Invoice invoice = request.getContext().asType(Invoice.class);

    if (invoice != null) {
        invoiceService.SetContractPaid(invoice);
    }
    else {
        throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.INVOICE_NOT_FOUND));
    }

    response.setValues(invoice);
    response.setReload(true);
}

}

