package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.Machine;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.MaintenanceMachineService;
import com.google.inject.Inject;

public class MaintenanceMachineController {
    @Inject private MaintenanceMachineService maintenanceMachineService;
    public void MaintenanceMachineValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Machine machine = request.getContext().asType(Machine.class);

        if (machine != null ) {
            maintenanceMachineService.Validate(machine);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_MACHINE_NOT_FOUND));
        }

        response.setValues(machine);
        response.setReload(true);
    }
}
