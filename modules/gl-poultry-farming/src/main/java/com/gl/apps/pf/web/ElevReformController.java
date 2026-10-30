package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ElevReform;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ElevReformService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class ElevReformController {
    @Inject
    private ElevReformService elevReformService;
    @Transactional
    public void ElevReformValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ElevReform elevReform = request.getContext().asType(ElevReform.class);

        if (elevReform != null ) {
            elevReformService.Validate(elevReform);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        response.setValues(elevReform);
        response.setReload(true);
    }
    @Transactional
    public void ElevReformCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ElevReform elevReform = request.getContext().asType(ElevReform.class);

        if (elevReform != null ) {
            elevReformService.Cancel(elevReform);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        response.setValues(elevReform);
        response.setReload(true);
    }

}

