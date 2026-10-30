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

import com.gl.apps.pf.db.Breed;
import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.BroilerGuide;
import com.gl.apps.pf.db.ProductionGuide;
import com.gl.apps.pf.db.StartBreedingGuide;

public interface BreedService {

  public void GenerateBreedGuides(Breed breed) throws AxelorException;
  public void GenerateBroilerGuides(Breed breed) throws AxelorException;
  public void GenerateProductionGuide(Breed breed) throws AxelorException;
  public Breed CalculateValuesBreed(Breed breed1) throws AxelorException;
  public Breed CalculateValuesBreedStartBreedingGuideList(StartBreedingGuide guide1) throws AxelorException;
  public Breed CalculateValuesDailyGainRateStartBreedingGuideList(StartBreedingGuide guide1) throws AxelorException;
  public Breed CalculateWeightValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException;
  public Breed CalculateConsumptionValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException;
  public Breed CalculateiCIndicatorValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException;
  public Breed CalculateTempValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException;
  public Breed CalculateHatchingRateProductionList(ProductionGuide guide1) throws AxelorException;
  public Breed CalculateweeklyEggsQuantityProductionList(ProductionGuide guide1) throws AxelorException;
  public Breed CalculateIncubateEggsNumberProductionList(ProductionGuide guide1) throws AxelorException;
  public Breed CalculateunitQtyEggsAtStartupProductionList(ProductionGuide guide1) throws AxelorException;
  public Breed CalculateEggsQtyByFemalePresentProductionList(ProductionGuide guide1) throws AxelorException;
  public Breed Test(ProductionGuide guide1) throws AxelorException;
  public Breed Test2(ProductionGuide guide1) throws AxelorException;
  public Breed Test3(ProductionGuide guide1) throws AxelorException;
  public Breed Test4(ProductionGuide guide1) throws AxelorException;
  public Breed Test5(ProductionGuide guide1) throws AxelorException;
  public Breed Test6(ProductionGuide guide1) throws AxelorException;
  public Breed Test7(ProductionGuide guide1) throws AxelorException;
}
