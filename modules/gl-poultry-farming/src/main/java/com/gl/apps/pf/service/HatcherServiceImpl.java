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
package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.Hatcher;
import com.gl.apps.pf.db.Hatchery;
import com.axelor.rpc.ActionRequest;
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.repo.HatcherRepository;
import com.gl.apps.pf.db.repo.HatcherTrolleyRepository;
import com.gl.apps.pf.db.repo.IncubatorRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

// import com.gl.apps.pf.db.PlanningRequest;

public class HatcherServiceImpl implements HatcherService {
  @Inject
  private HatcherRepository hatcherRepository;

  ActionRequest request;
  @Transactional
  public void StopHatcher(Hatcher entity) throws AxelorException {
    if (entity == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
    }
    entity.setReasonOfCancel(entity.getReasonOfCancel());
    entity.setStopDate(entity.getStopDate());
      if (!entity.getTrolleysList().isEmpty() ) {
        entity.getTrolleysList().forEach(
                line -> {
                  if (line != null) {
                    line.setStatus(HatcherTrolleyRepository.STATUS_STOPPED);
                  }
                });
      }
    entity.setStatus(HatcherRepository.STATUS_STOPPED);
    hatcherRepository.save(entity);
  }

  @Transactional
  public void FreeHatcher(Hatcher entity) throws AxelorException {
    if (entity == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
    }
    if (!entity.getTrolleysList().isEmpty() ) {
      entity.getTrolleysList().forEach(
              line -> {
                if (line != null) {
                  line.setStatus(IncubatorTrolleyRepository.STATUS_FREE);
                }
              });
    }
    entity.setStatus(HatcherRepository.STATUS_FREE);
    hatcherRepository.save(entity);
  }

  public void GenerateIncubatorsAndHatchers(Hatchery hatchery) {}

  @Override
  public Integer ShutDownHatcher(Integer status) throws AxelorException {
    Hatcher hatcher = request.getContext().asType(Hatcher.class);
    if (hatcher == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
    }
    if (status == 2) {
      hatcher.setStatus(1);
    }
    return status;
  }

  @Override
  public Integer StartUpHatcher(Integer status) throws AxelorException{
    Hatcher hatcher = request.getContext().asType(Hatcher.class);
    if (hatcher == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.HATCHER_NOT_FOUND));
    }
    if (status == 1) {
      hatcher.setStatus(2);
    }
    return status;
  }
}
