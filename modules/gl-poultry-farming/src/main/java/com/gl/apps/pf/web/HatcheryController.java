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

import com.axelor.meta.schema.actions.ActionView;
import com.gl.apps.pf.db.HatcherExit;
import com.gl.apps.pf.db.Hatchery;
import com.gl.apps.pf.db.repo.HatcherRepository;
import com.gl.apps.pf.db.repo.HatcheryRepository;
import com.gl.apps.pf.db.repo.IncubatorRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.HatcheryServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.util.Collections;
import java.util.List;

public class HatcheryController {
  @Inject private HatcheryRepository hatcheryRepository;
  @Inject private HatcherRepository hatcherRepository;
  @Inject private IncubatorRepository incubatorRepository;
  @Inject private HatcheryServiceImpl hatcheryService;

  @Transactional
  public void GenerateIncubatorsAndHatchers(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Hatchery hatchery = request.getContext().asType(Hatchery.class);

    if (hatchery.getId() != null) {
      hatchery = hatcheryRepository.find(hatchery.getId());
      String alertMsg = "";
      /*Incubators before Generator */
      long incubatorCount =
          incubatorRepository.all().filter("self.hatchery = ?1", hatchery).count();
      if (incubatorCount > hatchery.getNumberOfIncubators()) {
        alertMsg = I18n.get(PoultryFarmingExceptionMessage.INCUBATORS_REMOVE);
        response.setAlert(alertMsg);
      }
      long incubatorToRemove =
          incubatorRepository
              .all()
              .filter(
                  "self.hatchery = ?1 AND self.status != ?2",
                  hatchery,
                  IncubatorRepository.STATUS_BUSY)
              .count();

      if (incubatorToRemove < (incubatorCount - hatchery.getNumberOfIncubators())) {
        response.setError(I18n.get(PoultryFarmingExceptionMessage.INCUBATORS_TOBE_REMOVED));
      }

      /*Hatcher before Generator */
      long hatcherCount = hatcherRepository.all().filter("self.hatchery = ?1", hatchery).count();
      if (hatcherCount > hatchery.getNumberOfHatchers()) {
        alertMsg =
            alertMsg.concat(" ").concat(I18n.get(PoultryFarmingExceptionMessage.HATCHERS_REMOVE));
        response.setAlert(alertMsg.trim());
      }
      long hatcherToRemove =
          hatcherRepository
              .all()
              .filter(
                  "self.hatchery = ?1 AND self.status != ?2",
                  hatchery,
                  HatcherRepository.STATUS_BUSY)
              .count();

      if (hatcherToRemove < (hatcherCount - hatchery.getNumberOfHatchers())) {
        response.setError(I18n.get(PoultryFarmingExceptionMessage.HATCHERS_TOBE_REMOVED));
      }

      /*Call Generator */
      hatcheryService.GenerateIncubatorsAndHatchers(hatchery);

      response.setCanClose(true);
      response.setReload(true);
    }
  }
  @Transactional
  public void test(ActionRequest request, ActionResponse response) throws AxelorException {
    List<Object[]> result = hatcheryService.fetchRecords();
    response.setReload(true);
    response.setView(ActionView.define("data")
            .add("hatchery-dashboard-gridd")
            .context("context", Collections.singletonMap("data", result)).map()
    );
  }
}

