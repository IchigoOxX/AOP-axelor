package com.gl.apps.pos.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.pos.db.Shift;
import com.axelor.pos.db.repo.ShiftRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ShiftServiceImpl implements ShiftService {

    @Inject protected ShiftRepository shiftRepo;

    @Inject protected SaleOrderRepository saleOrderRepo;

    @Transactional
    public Shift updateShiftAfterSale(Long shiftId, Long saleOrderId) throws AxelorException {

        Shift shift = shiftRepo.find(shiftId);

        SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);
        shift.setNetSales(shift.getNetSales().add(saleOrder.getAmountInvoiced()));
        shift.setCashBalance(shift.getCashBalance().add(saleOrder.getAmountInvoiced()));
        shift.setTotalDiscounts(shift.getTotalDiscounts().add(saleOrder.getDiscountAmount()));
        shift.setTotalSales(shift.getNetSales().add(shift.getTotalDiscounts()));
        shift.setItemsSold(shift.getItemsSold()+saleOrder.getItemsCount());
        shift.setTotalTransactions(shift.getTotalTransactions() +1);
        shift.setNumberOfSales(shift.getNumberOfSales() +1);

        return shiftRepo.save(shift);
    }
}
