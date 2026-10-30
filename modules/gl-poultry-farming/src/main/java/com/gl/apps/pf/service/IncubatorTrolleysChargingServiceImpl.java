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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.IncubatorTrolleysCharging;
import com.gl.apps.pf.db.IncubatorTrolleysChargingLine;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleysChargingRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class IncubatorTrolleysChargingServiceImpl implements IncubatorTrolleysChargingService {

  protected IncubatorTrolleysChargingService incubatorTrolleysChargingService;
  @Inject private IncubatorTrolleysChargingRepository incubatorTrolleysChargingRepo;

  @Inject private AppBaseService appBaseService;

  @Transactional
  public IncubatorTrolleysCharging Validate(IncubatorTrolleysCharging incubatorTrolleysCharging) throws AxelorException {

    IncubatorTrolleysCharging incubatorTrolleysCharging1 = incubatorTrolleysChargingRepo.find(incubatorTrolleysCharging.getId());
    if (incubatorTrolleysCharging1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_CHARGING_NOT_FOUND));
    }
    if (!incubatorTrolleysCharging1.getIncubatorTrolleysChargingLineList().isEmpty()) {
            incubatorTrolleysCharging1.getIncubatorTrolleysChargingLineList().forEach(
              line -> {
                if (line != null) {
                  line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_BUSY);
                }
              });
    }
    incubatorTrolleysCharging1.setStatus(IncubatorTrolleysChargingRepository.STATUS_VALIDATED);

    return incubatorTrolleysChargingRepo.save(incubatorTrolleysCharging1);
  }

  @Transactional
  public IncubatorTrolleysCharging Cancel(IncubatorTrolleysCharging incubatorTrolleysCharging) throws AxelorException {

    IncubatorTrolleysCharging incubatorTrolleysCharging1 = incubatorTrolleysChargingRepo.find(incubatorTrolleysCharging.getId());
    if (incubatorTrolleysCharging1 == null) {

      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_CHARGING_NOT_FOUND));
    }

    if (!incubatorTrolleysCharging1.getIncubatorTrolleysChargingLineList().isEmpty()) {
      incubatorTrolleysCharging1.getIncubatorTrolleysChargingLineList().forEach(
            line -> {
              if (line != null) {
                line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_FREE);
              }
            });
    }
    incubatorTrolleysCharging1.setStatus(IncubatorTrolleysChargingRepository.STATUS_CANCELED);

    return incubatorTrolleysChargingRepo.save(incubatorTrolleysCharging1);
  }
}
