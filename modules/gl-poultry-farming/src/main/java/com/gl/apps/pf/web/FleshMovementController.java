package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.FleshMovementService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class FleshMovementController {
    @Inject private FleshMovementService fleshMovementService;
    public void getLinesFromBreedingShed(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshMovement movement = request.getContext().asType(FleshMovement.class);

        if (movement != null && movement.getFromBreedingShed() != null) {
            movement.clearFleshMovementLineList();

            int nbr = 1;
            for (BreedingShed x : movement.getFromCenter().getBreedingShedList()) {
                if ( x != movement.getFromBreedingShed() && x.getStatus() == BreedingShedRepository.STATUS_BUSY) {
                    FleshMovementLine rline = new FleshMovementLine();
                    rline.setLineNumber(nbr);
                    rline.setToBreedingShed(x);
                    movement.addFleshMovementLineListItem(rline);
                    nbr++;
                }
            }
        }
        response.setValues(movement);
    }

    @Transactional
    public void fleshMovementValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshMovement fleshMovement = request.getContext().asType(FleshMovement.class);

        if (fleshMovement != null ) {
            fleshMovementService.Validate(fleshMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_MOVEMENT_NOT_FOUND));
        }

        response.setValues(fleshMovement);
        response.setReload(true);
    }
    @Transactional
    public void fleshMovementCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshMovement fleshMovement = request.getContext().asType(FleshMovement.class);

        if (fleshMovement != null ) {
            fleshMovementService.Cancel(fleshMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_MOVEMENT_NOT_FOUND));
        }

        response.setValues(fleshMovement);
        response.setReload(true);
    }
}

