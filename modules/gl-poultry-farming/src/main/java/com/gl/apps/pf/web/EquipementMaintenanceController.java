package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MachineProductLine;
import com.axelor.apps.production.db.Machine;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class EquipementMaintenanceController {
    public void getLinesFromMachine(ActionRequest request, ActionResponse response)
            throws AxelorException {
        EquipementMaintenance equipementMaintenance = request.getContext().asType(EquipementMaintenance.class);

        if (equipementMaintenance != null) {
            equipementMaintenance.clearMachineProductLineList();

            for (MachineProductLine x : equipementMaintenance.getMachine().getSparePartProductsList()) {
                MachineProductLine rline = new MachineProductLine();
                rline.setProduct(x.getProduct());
                rline.setQty(x.getQty());
                rline.setTrackingNumber(x.getTrackingNumber());
                equipementMaintenance.addMachineProductLineListItem(rline);
            }
        }
        response.setValues(equipementMaintenance);
    }
}
