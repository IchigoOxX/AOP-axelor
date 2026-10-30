package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.HatcheryDeliverySale;
import com.gl.apps.pf.db.HatcheryDeliverySaleLine;
import com.gl.apps.pf.db.HatcheryEggExit;
import com.gl.apps.pf.db.repo.HatcheryDeliverySaleRepository;
import com.gl.apps.pf.db.repo.HatcheryEggExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcheryDeliverySaleService;
import com.gl.apps.pf.service.HatcheryEggExitService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class HatcheryDeliverySaleController {
    @Inject private HatcheryDeliverySaleService hatcheryDeliverySaleService;

    @Transactional
    public void HatcheryDeliverySaleValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryDeliverySale hatcheryDeliverySale = request.getContext().asType(HatcheryDeliverySale.class);

        if (hatcheryDeliverySale != null ) {
            hatcheryDeliverySaleService.Validate(hatcheryDeliverySale);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_DELIVERY_SALE_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryDeliverySale);
        response.setReload(true);
    }
    public void getLinesFromCustomerSaleOrder(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryDeliverySale hatcheryDeliverySale = request.getContext().asType(HatcheryDeliverySale.class);
        if (hatcheryDeliverySale != null && hatcheryDeliverySale.getCustomerSaleOrder() != null) {
            hatcheryDeliverySale.clearHatcheryDeliverySaleList();

            for (SaleOrderLine x : hatcheryDeliverySale.getCustomerSaleOrder().getSaleOrderLineList()) {
                if (x != null) {
                    HatcheryDeliverySaleLine rline = new HatcheryDeliverySaleLine();
                    rline.setProduct(x.getProduct());
                    rline.setCode(x.getProduct().getCode());
                    rline.setPurchaseUnit(x.getProduct().getUnit());
                    //rline.setAvailableQty(x.getProduct().get);
                    rline.setCapacity(x.getProduct().getCapacity());

                    rline.setDeliveredQty(x.getQty());
                    rline.setDeliveredQtyUnits((int) Math.ceil(x.getQty().doubleValue() / x.getProduct().getCapacity()));

                    hatcheryDeliverySale.addHatcheryDeliverySaleListItem(rline);
                }
            }
        }
        response.setValues(hatcheryDeliverySale);
    }

    @Transactional
    public void HatcheryDeliverySaleCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryDeliverySale hatcheryDeliverySale =
                Beans.get(HatcheryDeliverySaleRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (hatcheryDeliverySale != null) {
            hatcheryDeliverySale.setReasonOfCancel(cancelReason);
            hatcheryDeliverySaleService.Cancel(hatcheryDeliverySale);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_DELIVERY_SALE_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryDeliverySale);
        response.setReload(true);
    }

    @Transactional
    public void HatcheryDeliverySaleRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryDeliverySale hatcheryDeliverySale = request.getContext().asType(HatcheryDeliverySale.class);

        if (hatcheryDeliverySale != null ) {
            hatcheryDeliverySaleService.Revalidate(hatcheryDeliverySale);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_DELIVERY_SALE_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryDeliverySale);
        response.setReload(true);
    }
}

