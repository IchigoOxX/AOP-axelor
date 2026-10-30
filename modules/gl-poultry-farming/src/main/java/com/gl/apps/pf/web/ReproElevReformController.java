package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ReproElevReform;
import com.gl.apps.pf.db.StartElevReform;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ReproElevReformService;
import com.gl.apps.pf.service.StartElevReformService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class ReproElevReformController {
    @Inject
    private ReproElevReformService reproElevReformService;
    @Transactional
    public void ReproElevReformValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproElevReform elevReform = request.getContext().asType(ReproElevReform.class);

        if (elevReform != null ) {
            reproElevReformService.Validate(elevReform);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_REFORM_NOT_SPECIFIED));
        }

        response.setValues(elevReform);
        response.setReload(true);
    }

}

