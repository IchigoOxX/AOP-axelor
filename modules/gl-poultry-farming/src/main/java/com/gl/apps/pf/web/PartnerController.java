package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.PartnerService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class PartnerController {
    @Inject private PartnerService  partnerService;

    @Transactional
    public void AddComptInfo(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Partner partner = request.getContext().asType(Partner.class);

        if (partner != null ) {
            partnerService.AddComptInfo(partner);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Partner not found!"));
        }

        response.setValues(partner);
        response.setReload(true);
    }
}

