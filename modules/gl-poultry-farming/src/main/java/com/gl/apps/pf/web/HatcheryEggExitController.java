package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.HatcherExit;
import com.gl.apps.pf.db.HatcheryEggExit;
import com.gl.apps.pf.db.repo.HatcheryEggExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcheryEggExitService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class HatcheryEggExitController {
    @Inject private HatcheryEggExitService hatcheryEggExitService;
    @Transactional
    public void HatcheryEggExitValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggExit hatcheryEggExit = request.getContext().asType(HatcheryEggExit.class);

        if (hatcheryEggExit != null ) {
            hatcheryEggExitService.Validate(hatcheryEggExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryEggExit);
        response.setReload(true);
    }
    @Transactional
    public void HatcheryEggExitCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggExit hatcheryEggExit =
                Beans.get(HatcheryEggExitRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (hatcheryEggExit != null) {
            hatcheryEggExit.setReasonOfCancel(cancelReason);
            hatcheryEggExitService.Cancel(hatcheryEggExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryEggExit);
        response.setReload(true);
    }
    @Transactional
    public void HatcheryEggsExitRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggExit hatcheryEggExit = request.getContext().asType(HatcheryEggExit.class);

        if (hatcheryEggExit != null ) {
            hatcheryEggExitService.Revalidate(hatcheryEggExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_SPECIFIED));
        }

        response.setValues(hatcheryEggExit);
        response.setReload(true);
    }

}

