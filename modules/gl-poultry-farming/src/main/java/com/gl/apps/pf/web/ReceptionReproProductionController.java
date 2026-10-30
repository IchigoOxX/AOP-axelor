package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.ReceptionReproProduction;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ElevTransferService;
import com.gl.apps.pf.service.ReceptionReproProductionService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class ReceptionReproProductionController {
    @Inject
    private ReceptionReproProductionService receptReproService;
    @Transactional
    public void receptReproProductionValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReceptionReproProduction receptProduction = request.getContext().asType(ReceptionReproProduction.class);

        if (receptProduction != null ) {
            receptReproService.Validate(receptProduction);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        response.setValues(receptProduction);
        response.setReload(true);
    }
    @Transactional
    public void receptReproProductionCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ReceptionReproProduction receptProduction = request.getContext().asType(ReceptionReproProduction.class);

        if (receptProduction != null ) {
            receptReproService.Cancel(receptProduction);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEPTION_PRODUCTION_NOT_FOUND));
        }

        response.setValues(receptProduction);
        response.setReload(true);
    }


}

