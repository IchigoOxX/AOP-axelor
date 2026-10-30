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
import com.axelor.apps.base.service.exception.TraceBackService;
import com.gl.apps.pf.db.Hatcher;
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcherService;
import com.gl.apps.pf.service.HatcherServiceImpl;
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

public class HatcherController {
  @Inject private HatcheryRepository hatcheryRepository;
  @Inject private HatcherRepository hatcherRepository;
  @Inject private IncubatorRepository incubatorRepository;
  @Inject private HatcherServiceImpl hatcheryService;
  @Inject private HatcherService hatcherService;

  public void GetHatcherysStatus(ActionRequest request, ActionResponse response) {

    List<Hatcher> hatcherList = hatcherRepository.all().fetch();

    List<Map<String, Object>> dataList = new ArrayList<>();

    hatcherList.forEach(
        x -> {
          Map<String, Object> data = new HashMap<>();
          String status = x.getStatus() == 0 ? "Free" : x.getStatus() == 1 ? "Busy" : "Stopped";

          data.put("reference", x.getReference());
          data.put("trolleysList", x.getStrTrolleysList());
          data.put("statusDate", x.getStatus());
          data.put("status", I18n.get(status));

          dataList.add(data);
        });

    response.setData(dataList);
  }

  @Transactional
  public void ShutDownHatcher(ActionRequest request, ActionResponse response) {
    try {
      Hatcher hatcher = request.getContext().asType(Hatcher.class);
      if(hatcher == null ){
          throw new AxelorException(
                  TraceBackRepository.CATEGORY_MISSING_FIELD,
                  I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
      }

      Integer status = null;
      Integer shutDown = Beans.get(HatcherService.class).ShutDownHatcher(status);
      response.setValues(shutDown);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

    @Transactional
  public void StartUpHatcher(ActionRequest request, ActionResponse response) {
    try {
      Hatcher hatcher = request.getContext().asType(Hatcher.class);
        if(hatcher == null ){
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
        }
      Integer status = null;
      Integer shutDown = Beans.get(HatcherService.class).StartUpHatcher(status);
      response.setValues(shutDown);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
    @Transactional
    public void StopHatcher(ActionRequest request, ActionResponse response) {
        try {
            Hatcher hatcher =
                    Beans.get(HatcherRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            CancelReason cancelReason = (CancelReason) request.getContext().get("reasonOfCancel");
            LocalDateTime stopDate = (LocalDateTime) request.getContext().get("stopDate");
            if (hatcher != null) {
                hatcher.setReasonOfCancel(cancelReason);
                hatcher.setStopDate(stopDate);
                hatcherService.StopHatcher(hatcher);

            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
            }
            response.setReload(true);
            response.setValues(hatcher);

        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    @Transactional
    public void FreeHatcher(ActionRequest request, ActionResponse response) {
        try {
            Hatcher hatcher =
                    Beans.get(HatcherRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            if (hatcher != null) {
                hatcherService.FreeHatcher(hatcher);

            }
            response.setReload(true);
            response.setValues(hatcher);

        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }
}

