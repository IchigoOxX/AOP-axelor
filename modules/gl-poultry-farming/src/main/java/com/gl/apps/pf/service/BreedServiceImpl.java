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
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class BreedServiceImpl implements BreedService {

  @Inject
  private BreedRepository breedRepo;
  @Inject
  private StartBreedingGuideRepository startBreedingGuideRepo;
  @Inject
  private BroilerGuideRepository broilerGuideRepo;
  @Inject
  private ProductionGuideRepository productionGuideRepo;

  @Transactional
  public void GenerateBreedGuides(Breed breed) throws AxelorException {
    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }
    breed.clearStartBreedingGuideList();
    // startList.forEach(x -> startBreedingGuideRepo.remove(x));

    for (int i = 0; i < breed.getDepletionAge(); i++) {
      Map<String, Object> values = new HashMap<>();
      values.put("breed", breed);
      values.put("gender", StartBreedingGuideRepository.GENDER_MALE);
      values.put("dayNumber", i + 1);

      breed.addStartBreedingGuideListItem(startBreedingGuideRepo.create(values));
      // startBreedingGuideRepo.save(startBreedingGuideRepo.create(values));
      values.replace("gender", StartBreedingGuideRepository.GENDER_FEMALE);
      breed.addStartBreedingGuideListItem(startBreedingGuideRepo.create(values));
      // startBreedingGuideRepo.save(startBreedingGuideRepo.create(values));
    }
    breedRepo.save(breed);
  }

  @Transactional
  public void GenerateBroilerGuides(Breed breed) throws AxelorException {
    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }
    breed.clearBroilerGuideList();
    // startList.forEach(x -> startBreedingGuideRepo.remove(x));

    for (int i = 0; i < breed.getBroilerPeriodLength(); i++) {
      Map<String, Object> values = new HashMap<>();
      values.put("breed", breed);
      values.put("dayNumber", i + 1);

      breed.addBroilerGuideListItem(broilerGuideRepo.create(values));

    }
    breedRepo.save(breed);
  }

  @Transactional
  public void GenerateProductionGuide(Breed breed) throws AxelorException {
    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }
    breed.clearProductionGuideList();
    // startList.forEach(x -> startBreedingGuideRepo.remove(x));

    for (int i = breed.getFemaleAgeLayingStart() + 1; i < breed.getDepletionAge(); i++) {
      Map<String, Object> values = new HashMap<>();
      values.put("breed", breed);
      values.put("dayNumber", i);

      breed.addProductionGuideListItem(productionGuideRepo.create(values));

    }
    breedRepo.save(breed);

  }

  @Transactional
  public Breed CalculateValuesBreed(Breed breed1) throws AxelorException {

    Breed breed = breedRepo.find(breed1.getId());

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    if (breed.getId() != null) {
      breed = breedRepo.find(breed.getId());
      List<StartBreedingGuide> startBreedingGuideList = breed.getStartBreedingGuideList();

      //Calculate theoreticalWeightGain for F and M
      if (startBreedingGuideList != null && startBreedingGuideList.size() > 1) {
        List<StartBreedingGuide> filteredList = startBreedingGuideList.stream()
                .filter(guide -> guide.getGender().equals("M"))
                .collect(Collectors.toList());

        filteredList.sort(Comparator.comparingInt(StartBreedingGuide::getDayNumber));

        StartBreedingGuide previousGuide = filteredList.get(0);
        for (int i = 1; i < filteredList.size(); i++) {
          StartBreedingGuide currentGuide = filteredList.get(i);
          int theoreticalWeight = currentGuide.getTheoreticalWeight();
          int previousTheoreticalWeight = previousGuide.getTheoreticalWeight();
          int theoreticalWeightGain = theoreticalWeight - previousTheoreticalWeight;

          currentGuide.setTheoreticalWeightGain(theoreticalWeightGain);
          previousGuide = currentGuide;
        }
      }
      if (startBreedingGuideList != null && startBreedingGuideList.size() > 1) {
        List<StartBreedingGuide> filteredList = startBreedingGuideList.stream()
                .filter(guide -> guide.getGender().equals("F"))
                .collect(Collectors.toList());

        filteredList.sort(Comparator.comparingInt(StartBreedingGuide::getDayNumber));

        StartBreedingGuide previousGuide = filteredList.get(0);
        for (int i = 1; i < filteredList.size(); i++) {
          StartBreedingGuide currentGuide = filteredList.get(i);
          int theoreticalWeight = currentGuide.getTheoreticalWeight();
          int previousTheoreticalWeight = previousGuide.getTheoreticalWeight();
          int theoreticalWeightGain = theoreticalWeight - previousTheoreticalWeight;

          currentGuide.setTheoreticalWeightGain(theoreticalWeightGain);
          previousGuide = currentGuide;
        }
      }

      //Calculate the weeklyGainRate
      if (startBreedingGuideList != null && startBreedingGuideList.size() > 1) {
        Map<String, List<StartBreedingGuide>> genderGroups = startBreedingGuideList.stream()
                .collect(Collectors.groupingBy(StartBreedingGuide::getGender));

        genderGroups.forEach((gender, guides) -> {
          guides.sort(Comparator.comparingInt(StartBreedingGuide::getDayNumber));

          for (int i = 0; i < guides.size(); i += 7) {
            int weeklyGainRate = 0;
            for (int j = i; j < Math.min(i + 7, guides.size()); j++) {
              weeklyGainRate += guides.get(j).getDailyGainRate();
            }
            for (int j = i; j < Math.min(i + 7, guides.size()); j++) {
              guides.get(j).setWeeklyGainRate(weeklyGainRate);
            }
          }
        });
      }
    }

    return breedRepo.save(breed);
  }

  @Transactional
  public Breed CalculateValuesBreedStartBreedingGuideList(StartBreedingGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<StartBreedingGuide> startBreedingGuideList = breed.getStartBreedingGuideList();

    boolean updated = false;

    for (int i = 0; i < startBreedingGuideList.size(); i++) {
      StartBreedingGuide guide = startBreedingGuideList.get(i);

      if (!guide.getGender().equals(guide1.getGender())) {
        continue;
      }

      if ((guide1.getDayNumber() - 1) / 7 == 0) {
        // Set all lines in the first week with the same theoretical weight
        double theoreticalWeight = guide1.getTheoreticalWeight();
        for (StartBreedingGuide g : startBreedingGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 0 && g.getGender().equals(guide1.getGender())) {
            g.setTheoreticalWeight((int) theoreticalWeight);
          }
        }
        updated = true;
        break;
      } else if (guide1.getDayNumber() % 7 == 1) {
        // If the first day of the week
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        // Get theoretical weight of the last day of the previous week
        double previousWeekWeight = 0;
        for (StartBreedingGuide g : startBreedingGuideList) {
          if (g.getDayNumber() == previousWeekEndDay && g.getGender().equals(guide1.getGender())) {
            previousWeekWeight = g.getTheoreticalWeight();
            break;
          }
        }

        // Calculate the weight difference
        double weightDiff = guide1.getTheoreticalWeight() - previousWeekWeight;
        double dailyIncrease = weightDiff / 7;

        // Set the theoretical weight for lines
        double currentWeight = previousWeekWeight;
        for (int j = i; j < startBreedingGuideList.size(); j++) {
          StartBreedingGuide currentGuide = startBreedingGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == (guide1.getDayNumber() - 1) / 7
                  && currentGuide.getGender().equals(guide1.getGender())) {
            currentWeight += dailyIncrease;
            currentGuide.setTheoreticalWeight((int) currentWeight);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setStartBreedingGuideList(startBreedingGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified gender and day number"));
    }
  }


  @Transactional
  public Breed CalculateValuesDailyGainRateStartBreedingGuideList(StartBreedingGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<StartBreedingGuide> startBreedingGuideList = breed.getStartBreedingGuideList();

    boolean updated = false;

    for (int i = 0; i < startBreedingGuideList.size(); i++) {
      StartBreedingGuide guide = startBreedingGuideList.get(i);

      if (!guide.getGender().equals(guide1.getGender())) {
        continue;
      }

      if ((guide1.getDayNumber() - 1) / 7 == 0) {
        // Set all lines in the first week with the same GAIN RATE
        double dailyGainRate = guide1.getDailyGainRate();
        for (StartBreedingGuide g : startBreedingGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 0 && g.getGender().equals(guide1.getGender())) {
            g.setDailyGainRate((int) dailyGainRate);
          }
        }
        updated = true;
        break;
      } else if (guide1.getDayNumber() % 7 == 1) {
        // If the first day of the week
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        // Get GAIN RATE of the last day of the previous week
        double previousWeekDailyGainRate = 0;
        for (StartBreedingGuide g : startBreedingGuideList) {
          if (g.getDayNumber() == previousWeekEndDay && g.getGender().equals(guide1.getGender())) {
            previousWeekDailyGainRate = g.getDailyGainRate();
            break;
          }
        }

        // Calculate the gain rate difference
        double DailyGainRateDiff = guide1.getDailyGainRate() - previousWeekDailyGainRate;
        double dailyIncrease = DailyGainRateDiff / 7;

        // Set the theoretical gain rate for lines
        double currentDailyGainRate = previousWeekDailyGainRate;
        for (int j = i; j < startBreedingGuideList.size(); j++) {
          StartBreedingGuide currentGuide = startBreedingGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == (guide1.getDayNumber() - 1) / 7
                  && currentGuide.getGender().equals(guide1.getGender())) {
            currentDailyGainRate += dailyIncrease;
            currentGuide.setDailyGainRate((int) currentDailyGainRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setStartBreedingGuideList(startBreedingGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified gender and day number"));
    }
  }

  @Transactional
  public Breed CalculateHatchingRateProductionList(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {

      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> productionGuideList = breed.getProductionGuideList();

    boolean updated = false;

    int guide1Week = (guide1.getDayNumber() - 1) / 7;

    for (int i = 0; i < productionGuideList.size(); i++) {
      ProductionGuide guide = productionGuideList.get(i);

      int guideWeek = (guide.getDayNumber() - 1) / 7;

      if (guide1Week == 24 && guideWeek == 24) {
        BigDecimal hatchingRate = guide1.getHatchingRate();
        for (ProductionGuide g : productionGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 24) {
            g.setHatchingRate(hatchingRate);
          }
        }
        updated = true;
        break;
      } else if ((guide1.getDayNumber() - 1) % 7 == 0) {
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        BigDecimal previousWeekRate = BigDecimal.ZERO;
        for (ProductionGuide g : productionGuideList) {
          if (g.getDayNumber() == previousWeekEndDay) {
            previousWeekRate = g.getHatchingRate();
            break;
          }
        }

        BigDecimal rateDiff = guide1.getHatchingRate().subtract(previousWeekRate);
        BigDecimal dailyIncrease = rateDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);

        BigDecimal currentRate = previousWeekRate;

        for (int j = i; j < i + 7 && j < productionGuideList.size(); j++) {
          ProductionGuide currentGuide = productionGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == guide1Week) {
            currentRate = currentRate.add(dailyIncrease);
            currentGuide.setHatchingRate(currentRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setProductionGuideList(productionGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified day number"));
    }
  }

  @Transactional
  public Breed CalculateWeightValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<BroilerGuide> broilerGuideList = breed.getBroilerGuideList();

    int targetDayNumber = guide1.getDayNumber();
    double targetWeight = guide1.getWeight();

    if ((targetDayNumber - 1) / 7 == 0) {
      // First week case
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 0) {
          g.setWeight((int) targetWeight);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      double previousWeekWeight = 0;

      for (BroilerGuide g : broilerGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekWeight = g.getWeight();
          break;
        }
      }

      double weightDiff = targetWeight - previousWeekWeight;
      double dailyIncrease = weightDiff / 7;
      double currentWeight = previousWeekWeight;

      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentWeight += dailyIncrease;
          g.setWeight((int) currentWeight);
        }
      }
    }

    breed.setBroilerGuideList(broilerGuideList);
    return breedRepo.save(breed);
  }

  @Transactional
  public Breed CalculateConsumptionValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<BroilerGuide> broilerGuideList = breed.getBroilerGuideList();

    int targetDayNumber = guide1.getDayNumber();
    double targetConsump = guide1.getDailyConsumption();

    if ((targetDayNumber - 1) / 7 == 0) {
      // First week case
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 0) {
          g.setDailyConsumption((int) targetConsump);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      double previousWeekConspt = 0;

      for (BroilerGuide g : broilerGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekConspt = g.getDailyConsumption();
          break;
        }
      }

      double cosumpDiff = targetConsump - previousWeekConspt;
      double dailyIncrease = cosumpDiff / 7;
      double currentConsump = previousWeekConspt;

      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentConsump += dailyIncrease;
          g.setDailyConsumption((int) currentConsump);
        }
      }
    }

    breed.setBroilerGuideList(broilerGuideList);
    return breedRepo.save(breed);
  }

  @Transactional
  public Breed CalculateiCIndicatorValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<BroilerGuide> broilerGuideList = breed.getBroilerGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetIcIndicator = guide1.getiCIndicator();

    if ((targetDayNumber - 1) / 7 == 0) {
      // First week case
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 0) {
          g.setiCIndicator(targetIcIndicator);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (BroilerGuide g : broilerGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getiCIndicator();
          break;
        }
      }

      BigDecimal cosumpDiff = targetIcIndicator.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      broilerGuideList.sort(Comparator.comparingInt(BroilerGuide::getDayNumber));
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
            currentIndic = currentIndic.add(dailyIncrease);
            g.setiCIndicator(currentIndic);
        }
      }
    }

    breed.setBroilerGuideList(broilerGuideList);
    return breedRepo.save(breed);
  }

  @Transactional
  public Breed CalculateTempValuesBroilerGuideList(BroilerGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<BroilerGuide> broilerGuideList = breed.getBroilerGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetTemp = guide1.getSetTemperature();

    if ((targetDayNumber - 1) / 7 == 0) {
      // First week case
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 0) {
          g.setSetTemperature(targetTemp);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekTemp = BigDecimal.ZERO;

      for (BroilerGuide g : broilerGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekTemp = g.getSetTemperature();
          break;
        }
      }

      BigDecimal tempDiff = targetTemp.subtract(previousWeekTemp);
      BigDecimal dailyIncrease = tempDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentTemp = previousWeekTemp;

      broilerGuideList.sort(Comparator.comparingInt(BroilerGuide::getDayNumber));
      for (BroilerGuide g : broilerGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentTemp = currentTemp.add(dailyIncrease);
          g.setSetTemperature(currentTemp);
        }
      }
    }

    breed.setBroilerGuideList(broilerGuideList);
    return breedRepo.save(breed);
  }


  @Transactional
  public Breed CalculateweeklyEggsQuantityProductionList(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> productionGuideList = breed.getProductionGuideList();

    boolean updated = false;

    int guide1Week = (guide1.getDayNumber() - 1) / 7;

    for (int i = 0; i < productionGuideList.size(); i++) {
      ProductionGuide guide = productionGuideList.get(i);

      int guideWeek = (guide.getDayNumber() - 1) / 7;

      if (guide1Week == 24 && guideWeek == 24) {
        BigDecimal weeklyEggsQuantity = guide1.getWeeklyEggsQuantity();
        for (ProductionGuide g : productionGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 24) {
            g.setWeeklyEggsQuantity(weeklyEggsQuantity);
          }
        }
        updated = true;
        break;
      } else if ((guide1.getDayNumber() - 1) % 7 == 0) {
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        BigDecimal previousWeekRate = BigDecimal.ZERO;
        for (ProductionGuide g : productionGuideList) {
          if (g.getDayNumber() == previousWeekEndDay) {
            previousWeekRate = g.getWeeklyEggsQuantity();
            break;
          }
        }

        BigDecimal rateDiff = guide1.getWeeklyEggsQuantity().subtract(previousWeekRate);
        BigDecimal dailyIncrease = rateDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);

        BigDecimal currentRate = previousWeekRate;

        for (int j = i; j < i + 7 && j < productionGuideList.size(); j++) {
          ProductionGuide currentGuide = productionGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == guide1Week) {
            currentRate = currentRate.add(dailyIncrease);
            currentGuide.setWeeklyEggsQuantity(currentRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setProductionGuideList(productionGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified day number"));
    }
  }

  @Transactional
  public Breed CalculateIncubateEggsNumberProductionList(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> productionGuideList = breed.getProductionGuideList();

    boolean updated = false;

    int guide1Week = (guide1.getDayNumber() - 1) / 7;

    for (int i = 0; i < productionGuideList.size(); i++) {
      ProductionGuide guide = productionGuideList.get(i);

      int guideWeek = (guide.getDayNumber() - 1) / 7;

      if (guide1Week == 24 && guideWeek == 24) {
        BigDecimal incubateEggsNumber = guide1.getIncubateEggsNumber();
        for (ProductionGuide g : productionGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 24) {
            g.setIncubateEggsNumber(incubateEggsNumber);
          }
        }
        updated = true;
        break;
      } else if ((guide1.getDayNumber() - 1) % 7 == 0) {
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        BigDecimal previousWeekRate = BigDecimal.ZERO;
        for (ProductionGuide g : productionGuideList) {
          if (g.getDayNumber() == previousWeekEndDay) {
            previousWeekRate = g.getIncubateEggsNumber();
            break;
          }
        }

        BigDecimal rateDiff = guide1.getIncubateEggsNumber().subtract(previousWeekRate);
        BigDecimal dailyIncrease = rateDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);

        BigDecimal currentRate = previousWeekRate;

        for (int j = i; j < i + 7 && j < productionGuideList.size(); j++) {
          ProductionGuide currentGuide = productionGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == guide1Week) {
            currentRate = currentRate.add(dailyIncrease);
            currentGuide.setIncubateEggsNumber(currentRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setProductionGuideList(productionGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified day number"));
    }
  }

  @Transactional
  public Breed CalculateEggsQtyByFemalePresentProductionList(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> productionGuideList = breed.getProductionGuideList();

    boolean updated = false;

    int guide1Week = (guide1.getDayNumber() - 1) / 7;

    for (int i = 0; i < productionGuideList.size(); i++) {
      ProductionGuide guide = productionGuideList.get(i);

      int guideWeek = (guide.getDayNumber() - 1) / 7;

      if (guide1Week == 24 && guideWeek == 24) {
        BigDecimal eggsQtyByFemalePresent = guide1.getEggsQtyByFemalePresent();
        for (ProductionGuide g : productionGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 24) {
            g.setEggsQtyByFemalePresent(eggsQtyByFemalePresent);
          }
        }
        updated = true;
        break;
      } else if ((guide1.getDayNumber() - 1) % 7 == 0) {
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        BigDecimal previousWeekRate = BigDecimal.ZERO;
        for (ProductionGuide g : productionGuideList) {
          if (g.getDayNumber() == previousWeekEndDay) {
            previousWeekRate = g.getEggsQtyByFemalePresent();
            break;
          }
        }

        BigDecimal rateDiff = guide1.getEggsQtyByFemalePresent().subtract(previousWeekRate);
        BigDecimal dailyIncrease = rateDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);

        BigDecimal currentRate = previousWeekRate;

        for (int j = i; j < i + 7 && j < productionGuideList.size(); j++) {
          ProductionGuide currentGuide = productionGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == guide1Week) {
            currentRate = currentRate.add(dailyIncrease);
            currentGuide.setEggsQtyByFemalePresent(currentRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setProductionGuideList(productionGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified day number"));
    }
  }

  @Transactional
  public Breed CalculateunitQtyEggsAtStartupProductionList(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> productionGuideList = breed.getProductionGuideList();

    boolean updated = false;

    int guide1Week = (guide1.getDayNumber() - 1) / 7;

    for (int i = 0; i < productionGuideList.size(); i++) {
      ProductionGuide guide = productionGuideList.get(i);

      int guideWeek = (guide.getDayNumber() - 1) / 7;

      if (guide1Week == 24 && guideWeek == 24) {
        BigDecimal unitQtyEggsAtStartup = guide1.getUnitQtyEggsAtStartup();
        for (ProductionGuide g : productionGuideList) {
          if ((g.getDayNumber() - 1) / 7 == 24) {
            g.setUnitQtyEggsAtStartup(unitQtyEggsAtStartup);
          }
        }
        updated = true;
        break;
      } else if ((guide1.getDayNumber() - 1) % 7 == 0) {
        int previousWeekEndDay = guide1.getDayNumber() - 1;

        BigDecimal previousWeekRate = BigDecimal.ZERO;
        for (ProductionGuide g : productionGuideList) {
          if (g.getDayNumber() == previousWeekEndDay) {
            previousWeekRate = g.getUnitQtyEggsAtStartup();
            break;
          }
        }

        BigDecimal rateDiff = guide1.getUnitQtyEggsAtStartup().subtract(previousWeekRate);
        BigDecimal dailyIncrease = rateDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);

        BigDecimal currentRate = previousWeekRate;

        for (int j = i; j < i + 7 && j < productionGuideList.size(); j++) {
          ProductionGuide currentGuide = productionGuideList.get(j);
          if ((currentGuide.getDayNumber() - 1) / 7 == guide1Week) {
            currentRate = currentRate.add(dailyIncrease);
            currentGuide.setUnitQtyEggsAtStartup(currentRate);
          }
        }
        updated = true;
        break;
      }
    }

    if (updated) {
      breed.setProductionGuideList(productionGuideList);
      return breedRepo.save(breed);
    } else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("No matching guide found for the specified day number"));
    }
  }

  @Transactional
  public Breed Test(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetWeeklyEggsQuantity = guide1.getWeeklyEggsQuantity();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setWeeklyEggsQuantity(targetWeeklyEggsQuantity);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getWeeklyEggsQuantity();
          break;
        }
      }

      BigDecimal cosumpDiff = targetWeeklyEggsQuantity.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setWeeklyEggsQuantity(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }


  @Transactional
  public Breed Test2(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetIncubateEggsNumber = guide1.getIncubateEggsNumber();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setIncubateEggsNumber(targetIncubateEggsNumber);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getIncubateEggsNumber();
          break;
        }
      }

      BigDecimal cosumpDiff = targetIncubateEggsNumber.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setIncubateEggsNumber(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }

  @Transactional
  public Breed Test3(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetUnitQtyEggsAtStartup = guide1.getUnitQtyEggsAtStartup();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setUnitQtyEggsAtStartup(targetUnitQtyEggsAtStartup);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getUnitQtyEggsAtStartup();
          break;
        }
      }

      BigDecimal cosumpDiff = targetUnitQtyEggsAtStartup.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setUnitQtyEggsAtStartup(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }

  @Transactional
  public Breed Test4(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetEggsQtyByFemalePresent = guide1.getEggsQtyByFemalePresent();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setEggsQtyByFemalePresent(targetEggsQtyByFemalePresent);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getEggsQtyByFemalePresent();
          break;
        }
      }

      BigDecimal cosumpDiff = targetEggsQtyByFemalePresent.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setEggsQtyByFemalePresent(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }
  @Transactional
  public Breed Test5(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetHatchingEggsRate = guide1.getHatchingEggsRate();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setHatchingEggsRate(targetHatchingEggsRate);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getHatchingEggsRate();
          break;
        }
      }

      BigDecimal cosumpDiff = targetHatchingEggsRate.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setHatchingEggsRate(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }
  @Transactional
  public Breed Test6(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetEggWeight = guide1.getEggWeight();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setEggWeight(targetEggWeight);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getEggWeight();
          break;
        }
      }

      BigDecimal cosumpDiff = targetEggWeight.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setEggWeight(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }
  @Transactional
  public Breed Test7(ProductionGuide guide1) throws AxelorException {
    if (guide1 == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get("Guide not specified"));
    }

    Breed breed = guide1.getBreed();

    if (breed == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_NOT_SPECIFIED));
    }

    breed = breedRepo.find(breed.getId());
    List<ProductionGuide> prodGuideList = breed.getProductionGuideList();

    int targetDayNumber = guide1.getDayNumber();
    BigDecimal targetEggMass = guide1.getEggMass();

    if ((targetDayNumber - 1) / 7 == 24) {
      // First week case
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == 24) {
          g.setEggMass(targetEggMass);
        }
      }
    } else {
      // Subsequent weeks case
      int previousWeekEndDay = targetDayNumber - 1;
      BigDecimal previousWeekind = BigDecimal.ZERO;

      for (ProductionGuide g : prodGuideList) {
        if (g.getDayNumber() == previousWeekEndDay) {
          previousWeekind = g.getEggMass();
          break;
        }
      }

      BigDecimal cosumpDiff = targetEggMass.subtract(previousWeekind);
      BigDecimal dailyIncrease = cosumpDiff.divide(BigDecimal.valueOf(7), RoundingMode.HALF_UP);
      BigDecimal currentIndic = previousWeekind;

      prodGuideList.sort(Comparator.comparingInt(ProductionGuide::getDayNumber));
      for (ProductionGuide g : prodGuideList) {
        if ((g.getDayNumber() - 1) / 7 == (targetDayNumber - 1) / 7) {
          currentIndic = currentIndic.add(dailyIncrease);
          g.setEggMass(currentIndic);
        }
      }
    }

    breed.setProductionGuideList(prodGuideList);
    return breedRepo.save(breed);
  }
}