// <!--G-Link -->
package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.fleet.db.Vehicle;
import com.gl.apps.pf.db.TechnicalSheet;

public interface VehicleService {
    public Vehicle Vignette(Vehicle vehicle) throws AxelorException;
}
