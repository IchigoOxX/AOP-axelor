package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.IncubatorTrolleysChargingLine;
import com.gl.apps.pf.db.IncubatorTrolleysChargingLineDetail;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.google.inject.Inject;

public class IncubatorTrolleysChargingLineController {
    public void getLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorTrolleysChargingLine incubatorTrolleysChargingLine = request.getContext().asType(IncubatorTrolleysChargingLine.class);
        if (incubatorTrolleysChargingLine != null && incubatorTrolleysChargingLine.getCenter() != null) {
            incubatorTrolleysChargingLine.clearChargingLineDetailList();
            int nbr = 1;
            for (BreedingShed x : incubatorTrolleysChargingLine.getCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_REPRODUCTION && x.getStatus() == BreedingShedRepository.STATUS_BUSY) {
                    IncubatorTrolleysChargingLineDetail rline = new IncubatorTrolleysChargingLineDetail();
                    rline.setBreedingShed(x);
                    rline.setLineNumber(nbr);
                    incubatorTrolleysChargingLine.addChargingLineDetailListItem(rline);
                    nbr++;
                }
            }
        }
        response.setValues(incubatorTrolleysChargingLine);
    }

}
