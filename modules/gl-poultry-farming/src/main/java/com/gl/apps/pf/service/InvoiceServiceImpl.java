package com.gl.apps.pf.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoiceServiceImpl implements InvoiceService {
    @Inject private InvoiceRepository invoiceRepository;
    @Inject private ContractRepository contractRepository;
    @Transactional
    public Invoice SetContractPaid(Invoice invoice) throws AxelorException {
        Invoice invoice1 = invoiceRepository.find(invoice.getId());

        if (invoice1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INVOICE_NOT_FOUND));
        }
        if(invoice1.getContract()!= null ) {
            /* invoice1.getContract().setIsPaid(true); */
            contractRepository.save(invoice1.getContract());
        }
        return invoiceRepository.save(invoice1);
    }
}

