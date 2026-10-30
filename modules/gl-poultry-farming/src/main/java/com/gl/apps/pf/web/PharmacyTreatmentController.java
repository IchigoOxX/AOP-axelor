package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.ReceptionReproProduction;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.PharmacyTreatmentService;
import com.gl.apps.pf.service.ReceptionReproProductionService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class PharmacyTreatmentController {
    @Inject
    private PharmacyTreatmentService pharmacyTreatmentService;

    @Transactional
    public void pharmacyTreatmentValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        PharmacyTreatment treatment = request.getContext().asType(PharmacyTreatment.class);

        if (treatment != null ) {
            pharmacyTreatmentService.Validate(treatment);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PHARMACY_TREATMENT_NOT_FOUND));
        }

        response.setValues(treatment);
        response.setReload(true);
    }
}

