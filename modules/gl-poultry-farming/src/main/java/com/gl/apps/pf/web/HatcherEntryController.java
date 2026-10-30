package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.HatcherEntryLine;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorExitLine;
import com.gl.apps.pf.db.repo.HatcherEntryRepository;
import com.gl.apps.pf.db.repo.IncubatorExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcherEntryService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.Set;

public class HatcherEntryController {
    @Inject
    private HatcherEntryService hatcherEntryService;
    public void getLinesFromIncubatorExit(ActionRequest request, ActionResponse response)
            throws AxelorException {

        HatcherEntry hatcherEntry = request.getContext().asType(HatcherEntry.class);

        if (hatcherEntry != null && hatcherEntry.getIncubatorExit() != null) {
            hatcherEntry.clearHatcherEntryLinesList();

            for (IncubatorExitLine x : hatcherEntry.getIncubatorExit().getIncubatorExitLines()) {
                if (x.getIncubatorExit() == hatcherEntry.getIncubatorExit()
                        && x.getIncubatorExit().getStatus() == IncubatorExitRepository.STATUS_EXECUTING) {
                    HatcherEntryLine rline = new HatcherEntryLine();

                    rline.setIncubatorTrolley(x.getIncubatorTrolley());
                    rline.setBreedingShed(x.getBreedingShed());
                    rline.setCenter(x.getCenter());
                    rline.setTrackingNumber(x.getTrackingNumber());
                    rline.setCurrentDailyAge(x.getCurrentDailyAge());
                    rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
                    rline.setLoadingType(x.getLoadingType());
                    rline.setNumberOfOac(x.getEggsNumber() - x.getLostEggs());
                    rline.setNumberOfOacBasket((x.getEggsNumber() - x.getLostEggs()) / hatcherEntry.getHatchery().getCapacityOfAnEggBasket());
                    hatcherEntry.addHatcherEntryLinesListItem(rline);
                }
            }
        }
        response.setValues(hatcherEntry);
    }

    @Transactional
    public void HatcherEntryValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherEntry hatcherEntry = request.getContext().asType(HatcherEntry.class);

        if (hatcherEntry != null ) {
            hatcherEntryService.Validate(hatcherEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_SPECIFIED));
        }

        response.setValues(hatcherEntry);
        response.setReload(true);
    }
    @Transactional
    public void HatcherEntryCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherEntry hatcherEntry =
                Beans.get(HatcherEntryRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        if (hatcherEntry != null) {
            hatcherEntry.setReasonOfCancel(cancelReason);
            hatcherEntryService.Cancel(hatcherEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_SPECIFIED));
        }

        response.setValues(hatcherEntry);
        response.setReload(true);
    }
    @Transactional
    public void HatcherEntryRevalidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcherEntry hatcherEntry = request.getContext().asType(HatcherEntry.class);

        if (hatcherEntry != null ) {
           hatcherEntryService.Revalidate(hatcherEntry);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_SPECIFIED));
        }
        response.setValues(hatcherEntry);
        response.setReload(true);
    }

    public void checkMaxHatcher(ActionRequest request, ActionResponse response) throws AxelorException {
        HatcherEntry hatcherEntry = request.getContext().asType(HatcherEntry.class);

        if (hatcherEntry != null && hatcherEntry.getHatcherEntryLinesList() != null) {
            Set<Long> uniqueHatchers = new HashSet<>();

            for (HatcherEntryLine line : hatcherEntry.getHatcherEntryLinesList()) {
                if (line.getHatcher() != null) {
                    uniqueHatchers.add(line.getHatcher().getId());
                }
            }
            if (uniqueHatchers.size() > hatcherEntry.getHatchery().getNumberOfHatchersPerIncubator()) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_LINE_MAX_HATCHER));
            }
        }
    }

}

