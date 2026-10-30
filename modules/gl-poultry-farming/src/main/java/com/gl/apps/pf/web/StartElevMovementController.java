package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.StartElevMovementService;
import com.gl.apps.pf.service.StartTechnicalSheetService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StartElevMovementController {
    @Inject
    private StartElevMovementService startElevMovementService;

    @Transactional
    public void getLinesElevMovementFromBreedingShed(ActionRequest request, ActionResponse response)
            throws AxelorException {

        StartElevMovement elevMovement = request.getContext().asType(StartElevMovement.class);
        if (elevMovement != null && elevMovement.getCenter() != null) {
            elevMovement.clearStartElevMovementLineList();

            for (BreedingShed x : elevMovement.getCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_STARTUP && x.getStatus() == BreedingShedRepository.STATUS_BUSY
                        && x != elevMovement.getBreedingShed()) {
                    StartElevMovementLine rline = new StartElevMovementLine();
                    rline.setBreedingShed(x);
                    elevMovement.addStartElevMovementLineListItem(rline);
                }
            }
        }
        response.setValues(elevMovement);
    }

    @Transactional
    public void startElevMovValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartElevMovement startElevMovement = request.getContext().asType(StartElevMovement.class);

        if (startElevMovement != null ) {
            startElevMovementService.Validate(startElevMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_MOVEMENT_NOT_FOUND));
        }

        response.setValues(startElevMovement);
        response.setReload(true);
    }

    @Transactional
    public void startElevCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartElevMovement startElevMovement = request.getContext().asType(StartElevMovement.class);

        if (startElevMovement != null ) {
            startElevMovementService.Cancel(startElevMovement);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_MOVEMENT_NOT_FOUND));
        }

        response.setValues(startElevMovement);
        response.setReload(true);
    }

}
