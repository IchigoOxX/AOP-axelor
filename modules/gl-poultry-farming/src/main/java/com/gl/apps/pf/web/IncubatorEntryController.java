package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.repo.IncubatorEntryRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorEntryService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class IncubatorEntryController {
    @Inject
    private IncubatorEntryService incubatorEntryService;
    @Transactional
    public void IncubatorEntryValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorEntry incubatorEntry = request.getContext().asType(IncubatorEntry.class);

        if (incubatorEntry != null ) {
            incubatorEntryService.Validate(incubatorEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }

        response.setValues(incubatorEntry);
        response.setReload(true);
    }

    @Transactional
    public void IncubatorEntryCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {

        IncubatorEntry incubatorEntry =
                Beans.get(IncubatorEntryRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (incubatorEntry != null) {
            incubatorEntry.setReasonOfCancel(cancelReason);
            incubatorEntryService.Cancel(incubatorEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }
        response.setValues(incubatorEntry);
        response.setReload(true);
    }
    @Transactional
    public void IncubatorEntryRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorEntry incubatorEntry = request.getContext().asType(IncubatorEntry.class);

        if (incubatorEntry != null ) {
            incubatorEntryService.Revalidate(incubatorEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_ENTRY_NOT_SPECIFIED));
        }

        response.setValues(incubatorEntry);
        response.setReload(true);
    }

}

