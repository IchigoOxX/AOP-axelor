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

import com.axelor.apps.stock.db.StockMoveLine;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.ReproProductionLine;
import com.gl.apps.pf.db.StartElevMovementLine;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.StartElevMovementRepository;
import com.gl.apps.pf.service.ReproProductionService;
import com.axelor.apps.base.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ReproProductionController {
  @Inject private ReproProductionService reproProductionService;
  @Inject private BreedingShedRepository breedingShedRepository;

  public void getLinesFromCenter(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ReproProduction production = request.getContext().asType(ReproProduction.class);
    if (production != null && production.getCenter() != null) {
      production.clearReproProductionLineList();
      int nbr = 1;
      for (BreedingShed x : production.getCenter().getBreedingShedList()) {
        if (x.getType() == BreedingShedRepository.TYPE_REPRODUCTION && x.getStatus() == BreedingShedRepository.STATUS_BUSY) {
          ReproProductionLine rline = new ReproProductionLine();
          rline.setBreedingShed(x);
          rline.setLineNumber(nbr);
          production.addReproProductionLineListItem(rline);
          nbr++;
        }
      }
    }
    response.setValues(production);
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public void eggProductionValidate(ActionRequest request, ActionResponse response)
      throws AxelorException {

    ReproProduction reception = request.getContext().asType(ReproProduction.class);

    if (reception != null && reception.getHatchery() != null && reception.getCenter() != null) {
    // Calculate total OAC in breeding shed
      if (reception.getReproProductionLineList() != null ) {
        for (BreedingShed x : breedingShedRepository.all().fetch()) {
          int sumNumberOfHe = 0;
          int eggsTotal = 0;
          for (ReproProductionLine line : reception.getReproProductionLineList()) {
            if (line != null && x == line.getBreedingShed()) {
              sumNumberOfHe = x.getSumNumberOfHe() + line.getTotalNumberOfHe();
              eggsTotal = x.getSumTotalEggs() + line.getTotalEggs();
            }
          }
          x.setSumNumberOfHe(sumNumberOfHe);
          x.setSumTotalEggs(eggsTotal);
        }
      }

          reproProductionService.Validate(reception);
    }
    response.setValues(reception);
    response.setReload(true);
  }
  @Transactional
  public void eggProductionCancel(ActionRequest request, ActionResponse response)
          throws AxelorException {

    ReproProduction reception = request.getContext().asType(ReproProduction.class);

    if (reception != null && reception.getHatchery() != null && reception.getCenter() != null) {

      if (reception.getReproProductionLineList() != null ) {
        for (BreedingShed x : breedingShedRepository.all().fetch()) {
          int sumNumberOfHe = 0;
          for (ReproProductionLine line : reception.getReproProductionLineList()) {
            if (line != null && x == line.getBreedingShed()) {
              sumNumberOfHe = x.getSumNumberOfHe() - line.getNumberOfHe();
            }
          }
          x.setSumNumberOfHe(sumNumberOfHe);
        }
      }
      reproProductionService.Cancel(reception);
    }
    response.setValues(reception);
    response.setReload(true);
  }

}

