package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.FleshChicksReception;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.FleshChicksReceptionService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class FleshChicksReceptionController {
    @Inject private FleshChicksReceptionService fleshChicksReceptionService;
    @Transactional
    public void fleshChicksReceptionValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshChicksReception chicksReception = request.getContext().asType(FleshChicksReception.class);

        if (chicksReception != null ) {
            fleshChicksReceptionService.Validate(chicksReception);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_NOT_FOUND));
        }

        response.setValues(chicksReception);
        response.setReload(true);
    }
    @Transactional
    public void fleshChicksReceptionCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshChicksReception chicksReception = request.getContext().asType(FleshChicksReception.class);

        if (chicksReception != null ) {
            fleshChicksReceptionService.Cancel(chicksReception);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_NOT_FOUND));
        }

        response.setValues(chicksReception);
        response.setReload(true);
    }
}

