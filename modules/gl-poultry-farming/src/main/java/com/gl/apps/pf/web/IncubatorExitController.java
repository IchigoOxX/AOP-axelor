package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorEntryLine;
import com.gl.apps.pf.db.IncubatorExit;
import com.gl.apps.pf.db.IncubatorExitLine;
import com.gl.apps.pf.db.repo.IncubatorEntryRepository;
import com.gl.apps.pf.db.repo.IncubatorExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorEntryService;
import com.gl.apps.pf.service.IncubatorExitService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class IncubatorExitController {
    @Inject
    private IncubatorExitService incubatorExitService;

    public void getLinesFromIncubatorEntry(ActionRequest request, ActionResponse response)
            throws AxelorException {

        IncubatorExit incubatorExit = request.getContext().asType(IncubatorExit.class);

        if (incubatorExit != null && incubatorExit.getIncubatorEntry() != null) {
            incubatorExit.clearIncubatorExitLines();

            for (IncubatorEntryLine x : incubatorExit.getIncubatorEntry().getIncubatorEntryLines()) {
                if (x.getIncubatorEntry() == incubatorExit.getIncubatorEntry() && x.getIncubatorEntry().getStatus() == IncubatorEntryRepository.STATUS_EXECUTING) {
                    IncubatorExitLine rline = new IncubatorExitLine();
                    rline.setIncubatorTrolley(x.getIncubatorTrolley());
                    rline.setBreedingShed(x.getBreedingShed());
                    rline.setCenter(x.getCenter());
                    rline.setStatus(x.getStatus());
                    rline.setTrackingNumber(x.getTrackingNumber());
                    rline.setLoadingType(x.getLoadingType());
                    rline.setCurrentDailyAge(x.getCurrentDailyAge());
                    rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
                    rline.setTraysNumber(x.getTraysNumber());
                    rline.setEggsNumber(x.getEggsNumber());
                    incubatorExit.addIncubatorExitLine(rline);
                }
            }
            incubatorExit.setDateTimeIncubatorExit(incubatorExit.getDateTimeIncubatorEntry().plusDays(incubatorExit.getIncubatorEntry().getHatchery().getNumberOfIncubationDays()).plusHours(incubatorExit.getIncubatorEntry().getHatchery().getNumberOfIncubationHours()));
        }
        response.setValues(incubatorExit);
    }

    @Transactional
    public void IncubatorExitValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorExit incubatorExit = request.getContext().asType(IncubatorExit.class);

        if (incubatorExit != null ) {
            incubatorExitService.Validate(incubatorExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_FOUND));
        }

        response.setValues(incubatorExit);
        response.setReload(true);
    }
    @Transactional
    public void IncubatorExitCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorExit incubatorExit =
                Beans.get(IncubatorExitRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (incubatorExit != null) {
            incubatorExit.setReasonOfCancel(cancelReason);
            incubatorExitService.Cancel(incubatorExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_SPECIFIED));
        }

        response.setValues(incubatorExit);
        response.setReload(true);
    }
    @Transactional
    public void IncubatorExitRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        IncubatorExit incubatorExit = request.getContext().asType(IncubatorExit.class);

        if (incubatorExit != null ) {
            incubatorExitService.Revalidate(incubatorExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_EXIT_NOT_SPECIFIED));
        }

        response.setValues(incubatorExit);
        response.setReload(true);
    }


}

