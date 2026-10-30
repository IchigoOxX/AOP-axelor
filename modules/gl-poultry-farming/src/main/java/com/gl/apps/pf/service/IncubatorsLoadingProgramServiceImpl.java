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
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.repo.IncubatorsLoadingProgramRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class IncubatorsLoadingProgramServiceImpl implements IncubatorsLoadingProgramService {

  protected IncubatorsLoadingProgramService incubatorsLoadingProgramService;
  @Inject private IncubatorsLoadingProgramRepository incubatorsLoadingProgramRepo;

  @Inject private AppBaseService appBaseService;

  @Transactional
  public IncubatorsLoadingProgram validate(IncubatorsLoadingProgram loadingProgramRequest) throws AxelorException {
    if (loadingProgramRequest == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.LOADING_PROGRAM_REQUEST_NOT_FOUND));
    }

    loadingProgramRequest.setIsValid(true);

    return incubatorsLoadingProgramRepo.save(loadingProgramRequest);
  }
}
