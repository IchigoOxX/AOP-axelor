// <!--G-Link -->
package com.gl.apps.pf.web;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.apps.fleet.db.repo.VehicleRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.TechnicalSheet;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.VehicleService;
import com.google.inject.Inject;

import java.time.LocalDate;

public class vehicleController {
    @Inject
    private VehicleService vehicleService;
    public void setVignetteVehicle(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Vehicle vehicle = request.getContext().asType(Vehicle.class);

        if (vehicle != null ) {
            vehicleService.Vignette(vehicle);
        }

        response.setValues(vehicle);
        response.setReload(true);
    }
}
