package com.gl.apps.pf.web;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.StartElevReform;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.StartElevReformService;
import com.google.inject.Inject;
import com.axelor.apps.base.AxelorException;
import com.google.inject.persist.Transactional;

public class StartElevReformController {
    @Inject private StartElevReformService startElevReformService;
    @Transactional
    public void ElevReformValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartElevReform elevReform = request.getContext().asType(StartElevReform.class);

        if (elevReform != null ) {
            startElevReformService.Validate(elevReform);
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

