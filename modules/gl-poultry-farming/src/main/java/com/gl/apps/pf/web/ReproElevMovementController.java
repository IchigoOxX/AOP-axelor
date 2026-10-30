package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ReproElevMovementService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReproElevMovementController {
    @Inject
    ReproElevMovementService reproElevMovementService;
    @Transactional
    public void getLinesReproElevMovementFromBreedingShed(ActionRequest request, ActionResponse response)
            throws AxelorException {

        ReproElevMovement elevMovement = request.getContext().asType(ReproElevMovement.class);
        if (elevMovement != null && elevMovement.getCenter() != null) {
            elevMovement.clearReproElevMovementLineList();

            for (BreedingShed x : elevMovement.getCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_REPRODUCTION && x.getStatus() == BreedingShedRepository.STATUS_BUSY
                        && x != elevMovement.getBreedingShed()) {
                    ReproElevMovementLine rline = new ReproElevMovementLine();
                    rline.setBreedingShed(x);
                    elevMovement.addReproElevMovementLineListItem(rline);
                }
            }
        }
        response.setValues(elevMovement);
    }

    @Transactional
    public void reproElevMovValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproElevMovement reproElevMovement = request.getContext().asType(ReproElevMovement.class);

        if (reproElevMovement != null ) {
            reproElevMovementService.Validate(reproElevMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_MOVEMENT_NOT_FOUND));
        }

        response.setValues(reproElevMovement);
        response.setReload(true);
    }

    @Transactional
    public void reproElevMovCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReproElevMovement reproElevMovement = request.getContext().asType(ReproElevMovement.class);

        if (reproElevMovement != null ) {
            reproElevMovementService.Cancel(reproElevMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_MOVEMENT_NOT_FOUND));
        }

        response.setValues(reproElevMovement);
        response.setReload(true);
    }
}
