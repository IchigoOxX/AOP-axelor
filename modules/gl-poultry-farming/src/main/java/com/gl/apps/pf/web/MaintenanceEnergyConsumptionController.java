package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.LivestockFeedProduction;
import com.gl.apps.pf.db.MaintenanceEnergyConsumption;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.LivestockFeedProductionService;
import com.gl.apps.pf.service.MaintenanceEnergyConsumptionService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class MaintenanceEnergyConsumptionController {
    @Inject
    private MaintenanceEnergyConsumptionService maintenanceEnergyConsumptionService;
    @Transactional
    public void MaintenanceEnergyConsumptionValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        MaintenanceEnergyConsumption consumption = request.getContext().asType(MaintenanceEnergyConsumption.class);

        if (consumption != null ) {
            maintenanceEnergyConsumptionService.Validate(consumption);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_ENERGY_CONSUMPTION_NOT_FOUND));
        }

        response.setValues(consumption);
        response.setReload(true);
    }
}

