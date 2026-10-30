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

import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedRepository;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.BreedServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.service.BreedingShedService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BreedController {
  @Inject
  private BreedRepository breedRepo;
  @Inject
  private BreedServiceImpl breedService;

  @Transactional
  public void GenerateBreedGuides(ActionRequest request, ActionResponse response)
          throws AxelorException {
    Breed breed = request.getContext().asType(Breed.class);

    if (breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      breedService.GenerateBreedGuides(breed);
      response.setCanClose(true);
      response.setReload(true);
    }
  }
  @Transactional
  public void GenerateBroilerGuides(ActionRequest request, ActionResponse response)
          throws AxelorException {
    Breed breed = request.getContext().asType(Breed.class);

    if (breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      breedService.GenerateBroilerGuides(breed);
      response.setCanClose(true);
      response.setReload(true);
    }
  }
  @Transactional
  public void GenerateProductionGuide(ActionRequest request, ActionResponse response)
          throws AxelorException {
    Breed breed = request.getContext().asType(Breed.class);

    if (breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      breedService.GenerateProductionGuide(breed);
      response.setCanClose(true);
      response.setReload(true);
    }
  }
  @Transactional
  public void filterBreedingStartGuide(ActionRequest request, ActionResponse response) {

    Breed breed = request.getContext().asType(Breed.class);
    List<StartBreedingGuide> filteredList = null;

    if (breed != null && breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      Object genderObject = request.getContext().get("gender");

      if (genderObject != null) {
        String gender = genderObject.toString();
        filteredList = breed.getStartBreedingGuideList()
                .stream()
                .filter(t -> gender.equals(t.getGender()))
                .collect(Collectors.toList());
      } else {
        filteredList = breed.getStartBreedingGuideList()
                .stream()
                .collect(Collectors.toList());
      }

    }

    response.setValue("startBreedingGuideList",filteredList);
  }
  @Transactional
  public void CalculateValuesBreed(ActionRequest request, ActionResponse response) throws AxelorException {
    Breed breed = request.getContext().asType(Breed.class);

    if (breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      breedService.CalculateValuesBreed(breed);
      response.setValues(breed);
    }
  }

  @Transactional
  public void CalculateValuesBreedStartBreedingGuideList(ActionRequest request, ActionResponse response) throws AxelorException {
    StartBreedingGuide startBreedingGuide = request.getContext().asType(StartBreedingGuide.class);

    if (startBreedingGuide != null) {
      breedService.CalculateValuesBreedStartBreedingGuideList(startBreedingGuide);
      response.setValues(startBreedingGuide);
      response.setReload(true);
    }
  }

  @Transactional
  public void CalculateValuesDailyGainRateStartBreedingGuideList(ActionRequest request, ActionResponse response) throws AxelorException {
    StartBreedingGuide startBreedingGuide = request.getContext().asType(StartBreedingGuide.class);

    if (startBreedingGuide != null) {
      breedService.CalculateValuesDailyGainRateStartBreedingGuideList(startBreedingGuide);
      response.setValues(startBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateHatchingRateProductionList(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.CalculateHatchingRateProductionList(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateValuesBroilerGuidePanelList(ActionRequest request, ActionResponse response) throws AxelorException {
    BroilerGuide broilerGuide = request.getContext().asType(BroilerGuide.class);

    if (broilerGuide != null) {
      breedService.CalculateWeightValuesBroilerGuideList(broilerGuide);
      response.setValues(broilerGuide);
      response.setReload(true);

    }
  }
  @Transactional
  public void CalculateConsumptionValuesBroilerGuideList(ActionRequest request, ActionResponse response) throws AxelorException {
    BroilerGuide broilerGuide = request.getContext().asType(BroilerGuide.class);

    if (broilerGuide != null) {
      breedService.CalculateConsumptionValuesBroilerGuideList(broilerGuide);
      response.setValues(broilerGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateiCIndicatorValuesBroilerGuideList(ActionRequest request, ActionResponse response) throws AxelorException {
    BroilerGuide broilerGuide = request.getContext().asType(BroilerGuide.class);

    if (broilerGuide != null) {
      breedService.CalculateiCIndicatorValuesBroilerGuideList(broilerGuide);
      response.setValues(broilerGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateTempValuesBroilerGuideList(ActionRequest request, ActionResponse response) throws AxelorException {
    BroilerGuide broilerGuide = request.getContext().asType(BroilerGuide.class);

    if (broilerGuide != null) {
      breedService.CalculateTempValuesBroilerGuideList(broilerGuide);
      response.setValues(broilerGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateWeeklyEggsQuantityProductionList(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.CalculateweeklyEggsQuantityProductionList(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }

  @Transactional
  public void CalculateIncubateEggsNumberProductionList(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.CalculateIncubateEggsNumberProductionList(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateunitQtyEggsAtStartupProductionList(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.CalculateunitQtyEggsAtStartupProductionList(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void CalculateEggsQtyByFemalePresentProductionList(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.CalculateEggsQtyByFemalePresentProductionList(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test2(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test2(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test3(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test3(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test4(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test4(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }

  @Transactional
  public void Test5(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test5(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test6(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test6(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }
  @Transactional
  public void Test7(ActionRequest request, ActionResponse response) throws AxelorException {
    ProductionGuide reproBreedingGuide = request.getContext().asType(ProductionGuide.class);

    if (reproBreedingGuide != null) {
      breedService.Test7(reproBreedingGuide);
      response.setValues(reproBreedingGuide);
      response.setReload(true);
    }
  }

}

