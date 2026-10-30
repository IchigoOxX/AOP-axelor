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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.IncubatorExit;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.repo.IncubatorsLoadingProgramRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.IncubatorsLoadingProgramService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class IncubatorsLoadingProgramController {
  @Inject private IncubatorsLoadingProgramRepository loadingProgramRepo;
  @Inject private IncubatorsLoadingProgramService loadingProgramService;

  @Transactional
  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

    IncubatorsLoadingProgram loadingProgram =
        request.getContext().asType(IncubatorsLoadingProgram.class);
    if(loadingProgram == null ){
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_LOADING_PROGRAM_NOT_FOUND));
    }

    if (loadingProgram.getId() != null) {
      loadingProgram = loadingProgramRepo.find(loadingProgram.getId());
      loadingProgramService.validate(loadingProgram);
      response.setValues(loadingProgram);
      response.setReload(true);
    }
  }

}

