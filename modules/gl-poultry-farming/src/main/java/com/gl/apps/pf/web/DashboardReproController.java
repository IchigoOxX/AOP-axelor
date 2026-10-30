package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.HatcherEntryLineRepository;
import com.gl.apps.pf.db.repo.HatcherExitLineRepository;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionLineRepository;
import com.gl.apps.pf.db.repo.IncubatorEntryLineRepository;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardReproController {
    @Inject private HatcheryEggsReceptionLineRepository hatcheryEggsReceptionLineRepository;
    @Inject private IncubatorEntryLineRepository incubatorEntryLineRepository;
    @Inject private HatcherEntryLineRepository hatcherEntryLineRepository;
    @Inject private HatcherExitLineRepository hatcherExitLineRepository;

    @Transactional
    public void getLinesReproEggsProduction(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardRepro> dashboardProd = new ArrayList<>();

        if(request.getContext().get("trackingNumber") == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No tracking number was specified !"));
        }

        Map<String, Object> trackingNumberMap = (Map<String, Object>) request.getContext().get("trackingNumber");
        Integer trackingNumberId = (Integer) trackingNumberMap.get("id");

        for (HatcheryEggsReceptionLine hatcheryEggsReceptionLine : hatcheryEggsReceptionLineRepository.all().filter("self.eggsReception.status = 2 AND self.eggsReception.trackingNumber.id = ?",trackingNumberId ).fetch()) {

            BreedingShed breedingShed = hatcheryEggsReceptionLine.getBreedingShed();
            TrackingNumber trackingNumber = hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber();

            Integer sumHe = hatcheryEggsReceptionLine.getReceivedNumberOfHe();
            Integer sumDye = hatcheryEggsReceptionLine.getReceivedNumberOfDye();
            Integer sumDe = hatcheryEggsReceptionLine.getReceivedNumberOfDe();


            boolean found = false;

            for (DashboardRepro dashboardRepro : dashboardProd) {
                if (dashboardRepro.getBreedingShed().equals(breedingShed) && dashboardRepro.getTrackingNumber().equals(trackingNumber)) {
                    dashboardRepro.setHe(dashboardRepro.getHe() + sumHe);
                    dashboardRepro.setDye(dashboardRepro.getDye() + sumDye);
                    dashboardRepro.setDe(dashboardRepro.getDe() + sumDe);
                    dashboardRepro.setTotal(dashboardRepro.getDe()+ dashboardRepro.getDye() + dashboardRepro.getHe());

                    dashboardRepro.setRateHe(BigDecimal.valueOf((float)dashboardRepro.getHe()/dashboardRepro.getTotal()));
                    dashboardRepro.setRateDe(BigDecimal.valueOf((float)dashboardRepro.getDe()/dashboardRepro.getTotal()));
                    dashboardRepro.setRateDye(BigDecimal.valueOf((float)dashboardRepro.getDye()/dashboardRepro.getTotal()));
                    found = true;
                    break;
                }
            }

            if (!found) {
                DashboardRepro dashboardRepro = new DashboardRepro();
                dashboardRepro.setCenter(breedingShed.getCenter());
                dashboardRepro.setBreedingShed(breedingShed);
                dashboardRepro.setTrackingNumber(trackingNumber);
                dashboardRepro.setHe(sumHe);
                dashboardRepro.setDye(sumDye);
                dashboardRepro.setDe(sumDe);
                dashboardRepro.setTotal(sumHe+sumDye+sumDe);
                dashboardRepro.setRateHe(BigDecimal.valueOf((float)sumHe / (sumHe + sumDye + sumDe)));
                dashboardRepro.setRateDe(BigDecimal.valueOf((float)sumDe/(sumHe+sumDye+sumDe)));
                dashboardRepro.setRateDye(BigDecimal.valueOf((float)sumDye/(sumHe+sumDye+sumDe)));


                dashboardProd.add(dashboardRepro);
            }
        }


        response.setValue("reproductionDashboardList", dashboardProd);
    }



}

