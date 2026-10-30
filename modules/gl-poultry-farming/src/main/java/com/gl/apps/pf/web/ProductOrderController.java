package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.ProductOrder;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.PharmacyTreatmentService;
import com.gl.apps.pf.service.ProductOrderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductOrderController {
    @Inject
    private ProductOrderService productOrderService;
    @Transactional
    public void productOrderSend(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.Send(order);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderCancelSend(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.CancelSend(order);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderSendLiveStockFeedFactory(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.SendLiveStockFeedFactory(order);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderCancelSendLiveStockFeedFactory(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.CancelSendLiveStockFeedFactory(order);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }


    @Transactional
    public void productOrderValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.Validate(order);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderReceive(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.Receive(order);
        }else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }


        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderCancelReceive(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.CancelReceive(order);
        }else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }
    @Transactional
    public void productOrderCancelRelease(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ProductOrder order = request.getContext().asType(ProductOrder.class);

        if (order != null ) {
            productOrderService.CancelReleaseOrder(order);
        } else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        response.setValues(order);
        response.setReload(true);
    }


}
