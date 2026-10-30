/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.repo.IncubatorRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.ZonedDateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncubatorController {
  @Inject private IncubatorRepository incubatorRepository;
  @Inject private IncubatorService incubatorService;

  public void GetIncubatorsStatus(ActionRequest request, ActionResponse response) {

    List<Incubator> incubatorList = incubatorRepository.all().fetch();

    List<Map<String, Object>> dataList = new ArrayList<>();

    incubatorList.forEach(
        x -> {
          Map<String, Object> data = new HashMap<>();
          String status = x.getStatus() == 0 ? "Free" : x.getStatus() == 1 ? "Busy" : "Stopped";

          data.put("reference", x.getReference());
          data.put("hatchery", x.getHatchery());
          data.put("statusDate", x.getStatus());
          data.put("status", I18n.get(status));
          data.put("trolleysList", x.getStrTrolleysList());
          data.put("exitDate", x.getExitDate());

          dataList.add(data);
        });

    response.setData(dataList);
  }

  @Transactional
  public void StopIncubator(ActionRequest request, ActionResponse response) {
    try {
        Incubator incubator =
                Beans.get(IncubatorRepository.class)
                        .find(Long.parseLong(request.getContext().get("_id").toString()));
        CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
        LocalDateTime stopDate = (LocalDateTime) request.getContext().get("stopDate");
        if (incubator != null) {
            incubator.setReasonOfCancel(cancelReason);
            incubator.setStopDate(stopDate);
            incubatorService.StopIncubator(incubator);
        }
        response.setReload(true);
        response.setValues(incubator);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
    @Transactional
    public void FreeIncubator(ActionRequest request, ActionResponse response) {
        try {
            Incubator incubator =
                    Beans.get(IncubatorRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            if (incubator != null) {
                incubatorService.FreeIncubator(incubator);

            }
            response.setReload(true);
            response.setValues(incubator);

        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }
    @Transactional
      public void StartUpIncubator(ActionRequest request, ActionResponse response) {
        try {
          Incubator incubator = request.getContext().asType(Incubator.class);
          if(incubator == null) {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_NOT_FOUND));
          }
          Beans.get(IncubatorService.class).StartUpIncubator(incubator);
          response.setReload(true);

        } catch (Exception e) {
          TraceBackService.trace(response, e);
        }
        }
}

