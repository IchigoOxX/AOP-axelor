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

import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.inject.Beans;
import com.gl.apps.pf.db.HatcheryEggsReception;
import com.gl.apps.pf.db.HatcheryEggsReceptionLine;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.service.HatcheryEggsReceptionService;
import com.gl.apps.pf.service.HatcheryEggsReceptionServiceImpl;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.apps.base.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HatcheryEggsReceptionController {

  @Inject private HatcheryEggsReceptionService eggsReceptionService;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * call check duplicate wizard
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void callCheckDuplicateReproProduction(ActionRequest request, ActionResponse response) {

    LOG.debug("Call check duplicate wizard for model : {} ", request.getModel());

    Date dateTime = getDateTime(request, (Class<? extends Model>) JPA.model(request.getModel()));
    Integer center = getCenter(request, (Class<? extends Model>) JPA.model(request.getModel()));
    Integer breed = getBreed(request, (Class<? extends Model>) JPA.model(request.getModel()));
    Integer hatchery = getHatchery(request, (Class<? extends Model>) JPA.model(request.getModel()));

    response.setView(
        ActionView.define("Check duplicate")
            .model(ReproProduction.class.getName())
            .add("grid", "hatchery-eggs-reception-parametre-grid")
            .param("popup", "true")
            .param("show-toolbar", "true")
            .param("popup-save", "false")
            .context("_reference", request.getModel())
            .context("_dateTime", dateTime)
            .context("_center", center)
            .context("_breed", breed)
            .context("_hatchery", hatchery)
            .map());
  }

  private Date getDateTime(ActionRequest request, Class<? extends Model> modelClass) {

    Date dateTime = (Date) request.getContext().get("_dateTime");
    return dateTime;
  }

  private Integer getCenter(ActionRequest request, Class<? extends Model> modelClass) {

    Integer center = (Integer) request.getContext().get("_center");
    return center;
  }

  private Integer getBreed(ActionRequest request, Class<? extends Model> modelClass) {

    Integer breed = (Integer) request.getContext().get("_breed");
    return breed;
  }

  private Integer getHatchery(ActionRequest request, Class<? extends Model> modelClass) {

    Integer hatchery = (Integer) request.getContext().get("_hatchery");
    return hatchery;
  }

  public void getLinesFromPrduction(ActionRequest request, ActionResponse response)
      throws AxelorException {
    HatcheryEggsReception reception = request.getContext().asType(HatcheryEggsReception.class);
    if (reception != null && reception.getReproProduction() != null) {
      reception.clearHatcheryEggsReceptionLineList();
      reception
          .getReproProduction()
          .getReproProductionLineList()
          .forEach(
              line -> {
                HatcheryEggsReceptionLine rline = new HatcheryEggsReceptionLine();
                rline.setBreedingShed(line.getBreedingShed());
                rline.setLineNumber(line.getLineNumber().toString());
                rline.setNumberOfHeTrays(line.getNumberOfHeTrays());
                rline.setNumberOfHe(line.getNumberOfHe());
                rline.setNumberOfDeTrays(line.getNumberOfDETrays());
                rline.setNumberOfDyeTrays(line.getNumberOfDYETrays());
                rline.setNumberOfDe(line.getNumberOfDE());
                rline.setNumberOfDye(line.getNumberOfDYE());
                rline.setNumberOfRe(line.getNumberOfRE());
                reception.addHatcheryEggsReceptionLineListItem(rline);
              });
    }
    response.setValues(reception);
  }

  public void eggReceptionValidate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    HatcheryEggsReception reception = request.getContext().asType(HatcheryEggsReception.class);

    if (reception != null && reception.getReproProduction() != null) {
      eggsReceptionService.Validate(reception);
    }
    response.setReload(true);
    response.setValues(reception);
  }
    public void eggReceptionConfirmStock(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggsReception reception = request.getContext().asType(HatcheryEggsReception.class);

        if (reception != null ) {
            eggsReceptionService.ConfirmStock(reception);
        }
        response.setReload(true);
        response.setValues(reception);
    }
    public void eggReceptionCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        HatcheryEggsReception reception = request.getContext().asType(HatcheryEggsReception.class);

        if (reception != null ) {
            eggsReceptionService.Cancel(reception);
        }
        response.setReload(true);
        response.setValues(reception);
    }


}
