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
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.repo.IncubatorRepository;
import com.axelor.rpc.ActionRequest;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class IncubatorServiceImpl implements IncubatorService {
  @Inject private IncubatorRepository incubatorRepository;
  ActionRequest request;

  @Transactional
  public void StopIncubator(Incubator entity) throws AxelorException {
    if (entity == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_NOT_FOUND));
    }
    if (!entity.getTrolleysList().isEmpty() ) {
      entity.getTrolleysList().forEach(
              line -> {
                if (line != null) {
                  line.setStatus(IncubatorTrolleyRepository.STATUS_STOPPED);
                }
              });
    }
    entity.setReasonOfCancel(entity.getReasonOfCancel());
    entity.setStopDate(entity.getStopDate());
    entity.setStatus(IncubatorRepository.STATUS_STOPPED);
    incubatorRepository.save(entity);
  }

  @Transactional
  public void FreeIncubator(Incubator entity) throws AxelorException {
    if (entity == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_NOT_FOUND));
    }
    if (!entity.getTrolleysList().isEmpty() ) {
      entity.getTrolleysList().forEach(
              line -> {
                if (line != null) {
                  line.setStatus(IncubatorTrolleyRepository.STATUS_FREE);
                }
              });
    }
    entity.setStatus(IncubatorRepository.STATUS_FREE);
    incubatorRepository.save(entity);
  }

  @Override
  public void StartUpIncubator(Incubator entity) throws AxelorException{
    if (entity == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_NOT_FOUND));
    }
    if( entity.getStatus() == IncubatorRepository.STATUS_BUSY) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Incubator already busy !"));
    }
    entity.setStatus(IncubatorRepository.STATUS_BUSY);
    incubatorRepository.save(entity);
  }
}
