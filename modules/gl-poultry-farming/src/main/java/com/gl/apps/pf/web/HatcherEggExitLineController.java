package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.HatcheryEggExitLine;
import com.gl.apps.pf.db.HatcheryEggExitLineDetail;
import com.gl.apps.pf.db.repo.BreedingShedRepository;

public class HatcherEggExitLineController {

    public void getLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggExitLine hatcheryEggExitLine = request.getContext().asType(HatcheryEggExitLine.class);
        if (hatcheryEggExitLine != null && hatcheryEggExitLine.getCenter() != null) {
            hatcheryEggExitLine.clearHatcheryEggExitLineDetail();

            for (BreedingShed x : hatcheryEggExitLine.getCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_REPRODUCTION && x.getStatus() == BreedingShedRepository.STATUS_BUSY) {
                    HatcheryEggExitLineDetail rline = new HatcheryEggExitLineDetail();
                    rline.setBreedingShed(x);
                    rline.setNumberOfExistentEggs(x.getSumNumberOfHe());
                    rline.setNumberOfExistentEggTrays((int) Math.ceil((double) x.getSumNumberOfHe() / 30));
                    hatcheryEggExitLine.addHatcheryEggExitLineDetail(rline);
                }
            }
        }
        response.setValues(hatcheryEggExitLine);
    }

}
