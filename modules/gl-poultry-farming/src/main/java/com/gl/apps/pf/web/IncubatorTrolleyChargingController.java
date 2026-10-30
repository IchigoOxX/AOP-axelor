package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.IncubatorTrolleysCharging;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorTrolleysChargingService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class IncubatorTrolleyChargingController {
    @Inject
    private IncubatorTrolleysChargingService incubatorTrolleysChargingService;
    @Transactional
    public void IncubatorTrolleyChargValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorTrolleysCharging incubatorTrolleysCharging = request.getContext().asType(IncubatorTrolleysCharging.class);

        if (incubatorTrolleysCharging != null ) {
            incubatorTrolleysChargingService.Validate(incubatorTrolleysCharging);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_CHARGING_NOT_SPECIFIED));
        }

        response.setValues(incubatorTrolleysCharging);
        response.setReload(true);
    }
    @Transactional
    public void IncubatorTrolleyChargCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorTrolleysCharging incubatorTrolleysCharging = request.getContext().asType(IncubatorTrolleysCharging.class);

        if (incubatorTrolleysCharging != null ) {
            incubatorTrolleysChargingService.Cancel(incubatorTrolleysCharging);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_CHARGING_NOT_SPECIFIED));
        }

        response.setValues(incubatorTrolleysCharging);
        response.setReload(true);
    }


}

