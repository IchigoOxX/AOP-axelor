package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.FleshLoadingProgramRepository;
import com.gl.apps.pf.db.repo.HatcherExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.FleshLoadingProgramService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class FleshLoadingProgramController {
    @Inject private FleshLoadingProgramService fleshLoadingProgramService;
    public void getLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshLoadingProgram loadingProgram = request.getContext().asType(FleshLoadingProgram.class);
        if (loadingProgram != null && loadingProgram.getCenter() != null) {
            loadingProgram.clearFleshLoadingProgramLineList();

            for (BreedingShed x : loadingProgram.getCenter().getBreedingShedList()) {
                FleshLoadingProgramLine rline = new FleshLoadingProgramLine();
                rline.setBreedingShed(x);
                loadingProgram.addFleshLoadingProgramLineListItem(rline);
            }
        }
        response.setValues(loadingProgram);
    }
    @Transactional
    public void loadingProgramActivateBS(ActionRequest request, ActionResponse response)
            throws AxelorException {
        FleshLoadingProgram fleshLoadingProgram = request.getContext().asType(FleshLoadingProgram.class);
        if (fleshLoadingProgram != null ) {
            fleshLoadingProgramService.ActivateBreedingShed(fleshLoadingProgram);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));
        }

        response.setValues(fleshLoadingProgram);
        response.setReload(true);
    }

    @Transactional
    public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

        FleshLoadingProgram fleshLoadingProgram = request.getContext().asType(FleshLoadingProgram.class);
        if (fleshLoadingProgram != null) {
            fleshLoadingProgramService.validate(fleshLoadingProgram);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));
        }
        response.setReload(true);
    }
}

