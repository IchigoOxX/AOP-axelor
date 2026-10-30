package com.gl.apps.pf.web;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.gl.apps.pf.db.IncubatorTrolley;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorService;
import com.gl.apps.pf.service.IncubatorTrolleyService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.util.LinkedHashMap;

public class IncubatorTrolleyController {
@Inject private IncubatorTrolleyRepository incubatorTrolleyRepository;
@Inject private IncubatorTrolleyService incubatorTrolleyService;
    @Transactional
    public void stop(ActionRequest request, ActionResponse response) {
        try {
            Context context = request.getContext();
            Long incubatorTrolleyId = Long.valueOf(context.get("_id").toString());
            LinkedHashMap<String, Object> saleOrderTemplateContext =(LinkedHashMap<String, Object>) request.getContext().get("incubatorTrolley");
            Long newIncubatorTrolleyId = Long.valueOf(saleOrderTemplateContext.get("id").toString());

            IncubatorTrolley newIncubatorTrolley = Beans.get(IncubatorTrolleyRepository.class).find(Long.valueOf(newIncubatorTrolleyId));
            IncubatorTrolley incubatorTrolley = Beans.get(IncubatorTrolleyRepository.class).find(incubatorTrolleyId);

            incubatorTrolleyService.StopIncubatorTrolley(incubatorTrolley, newIncubatorTrolley);

            context.put("incubatorTrolley", incubatorTrolley);
            context.put("newIncubatorTrolley", newIncubatorTrolley);

            response.setValues(context);

            response.setReload(true);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }
}

