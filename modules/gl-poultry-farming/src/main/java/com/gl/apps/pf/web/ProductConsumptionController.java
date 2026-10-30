package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ProductConsumption;
import com.gl.apps.pf.db.ProductOrder;
import com.gl.apps.pf.db.repo.IncubatorEntryRepository;
import com.gl.apps.pf.db.repo.ProductConsumptionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ProductConsumptionService;
import com.gl.apps.pf.service.ProductOrderService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class ProductConsumptionController {
    @Inject
    private ProductConsumptionService productConsumptionService;
    @Transactional
    public void productConsumptionValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {

        ProductConsumption productConsumption = request.getContext().asType(ProductConsumption.class);
        if (productConsumption != null ) {
            productConsumptionService.Validate(productConsumption);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_FOUND));
        }

        response.setValues(productConsumption);
        response.setReload(true);
    }

    @Transactional
    public void productConsumptionCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {

        ProductConsumption productConsumption = request.getContext().asType(ProductConsumption.class);
        if (productConsumption != null ) {
            productConsumptionService.Cancel(productConsumption);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_FOUND));
        }

        response.setValues(productConsumption);
        response.setReload(true);
    }
}

