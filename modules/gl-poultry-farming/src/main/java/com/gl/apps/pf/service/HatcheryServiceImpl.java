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


import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.db.JPA;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
/* import com.axelor.utils.QueryBuilder; */
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HatcheryServiceImpl implements HatcheryService {

  @Inject private HatcheryRepository hatcheryRepository;
  @Inject private HatcherRepository hatcherRepository;
  @Inject private IncubatorRepository incubatorRepository;
  @Inject private IncubatorTrolleyRepository incubatorTrolleyRepository;
  @Inject private HatcherTrolleyRepository hatcherTrolleyRepository;


  @Transactional
  public void GenerateIncubatorsAndHatchers(Hatchery hatchery) throws AxelorException {
    GenerateIncubators(hatchery);
    GenerateHatchers(hatchery);
    GenerateIncubatorTrolley(hatchery);
    GenerateHatcherTrolley(hatchery);
  }

  private void GenerateIncubators(Hatchery hatchery) throws AxelorException {

    if(hatchery == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
    }

    List<Incubator> incubatorList =
        incubatorRepository.all().filter("self.hatchery = ?1", hatchery).fetch();
    Integer ln =
        incubatorList.stream()
            .map(n -> Integer.parseInt(n.getReference() != null ? n.getReference() : "0"))
            .max(Integer::compareTo)
            .orElse(0);

    if (incubatorList.stream().count() < hatchery.getNumberOfIncubators()) {
      long incToAdd = hatchery.getNumberOfIncubators() - incubatorList.stream().count();

      for (int i = 0; i < incToAdd; i++) {
        Map<String, Object> values = new HashMap<>();
        values.put("hatchery", hatchery);
        values.put("status", IncubatorRepository.STATUS_FREE);
        values.put("reference", String.format("%03d", ++ln));
        values.put("incubatorCapacityInTrolleys", hatchery.getIncubatorCapacityInTrolleys());
        Incubator inc = incubatorRepository.create(values);

        incubatorRepository.save(inc);
      }
    } else {
      long incToRmv = incubatorList.stream().count() - hatchery.getNumberOfIncubators();
      if (incToRmv > 0) {
        List<Incubator> incubatorToRmvList =
            incubatorRepository
                .all()
                .filter(
                    "self.hatchery = ?1 AND self.status != ?2",
                    hatchery,
                    IncubatorRepository.STATUS_BUSY)
                .fetch((int) incToRmv);
        if (incubatorToRmvList.stream().count() < incToRmv) {
          throw new AxelorException(
              hatchery,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATORS_TOBE_REMOVED),
              hatchery.getHatcheryRef());
        }
        incubatorToRmvList.forEach(x -> incubatorRepository.remove(x));
      }
    }
  }

  private void GenerateHatchers(Hatchery hatchery) throws AxelorException {

    if(hatchery == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
    }

    List<Hatcher> hatcherList =
        hatcherRepository.all().filter("self.hatchery = ?1", hatchery).fetch();
    Integer ln =
        hatcherList.stream()
            .map(n -> Integer.parseInt(n.getReference() != null ? n.getReference() : "0"))
            .max(Integer::compareTo)
            .orElse(0);

    if (hatcherList.stream().count() < hatchery.getNumberOfHatchers()) {
      long incToAdd = hatchery.getNumberOfHatchers() - hatcherList.stream().count();

      for (int i = 0; i < incToAdd; i++) {
        Map<String, Object> values = new HashMap<>();
        values.put("hatchery", hatchery);
        values.put("status", HatcherRepository.STATUS_FREE);
        values.put("reference", String.format("%03d", ++ln));
        values.put("capacityOfTheHatcherInTrolleys", hatchery.getCapacityOfTheHatcherInTrolleys());
        Hatcher inc = hatcherRepository.create(values);
        hatcherRepository.save(inc);
      }
    } else {
      long hatToRmv = hatcherList.stream().count() - hatchery.getNumberOfHatchers();
      if (hatToRmv > 0) {
        List<Hatcher> hatcherToRmvList =
            hatcherRepository
                .all()
                .filter(
                    "self.hatchery = ?1 AND self.status != ?2",
                    hatchery,
                    HatcherRepository.STATUS_BUSY)
                .fetch((int) hatToRmv);

        if (hatcherToRmvList.stream().count() < hatToRmv) {
          throw new AxelorException(
              hatchery,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERS_TOBE_REMOVED),
              hatchery.getHatcheryRef());
        }
        hatcherToRmvList.forEach(x -> hatcherRepository.remove(x));
      }
    }
  }

  private void GenerateIncubatorTrolley(Hatchery hatchery) throws AxelorException {

    if(hatchery == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
    }

    List<IncubatorTrolley> list =
        incubatorTrolleyRepository.all().filter("self.hatchery = ?1", hatchery).fetch();
    Integer ln =
        list.stream()
            .map(n -> Integer.parseInt(n.getReference() != null ? n.getReference() : "0"))
            .max(Integer::compareTo)
            .orElse(0);

    if (list.stream().count() < hatchery.getNumberOfIncubatorTrolleys()) {
      long incToAdd = hatchery.getNumberOfIncubatorTrolleys() - list.stream().count();

      for (int i = 0; i < incToAdd; i++) {
        Map<String, Object> values = new HashMap<>();
        values.put("hatchery", hatchery);
        values.put("status", IncubatorTrolleyRepository.STATUS_FREE);
        values.put("reference", String.format("%04d", ++ln));
        values.put("capacityOfATrolleyInTrays", hatchery.getCapacityOfATrolleyInTrays());
        IncubatorTrolley inc = incubatorTrolleyRepository.create(values);
        incubatorTrolleyRepository.save(inc);
      }
    } else {
      long hatToRmv = list.stream().count() - hatchery.getNumberOfIncubatorTrolleys();
      if (hatToRmv > 0) {
        List<IncubatorTrolley> curentList =
            incubatorTrolleyRepository
                .all()
                .filter(
                    "self.hatchery = ?1 AND self.status != ?2 AND self.status != ?3",
                    hatchery,
                    IncubatorTrolleyRepository.STATUS_BUSY,
                    IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR)
                .fetch((int) hatToRmv);

        if (curentList.stream().count() < hatToRmv) {
          throw new AxelorException(
              hatchery,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_TOBE_REMOVED),
              hatchery.getHatcheryRef());
        }
        curentList.forEach(x -> incubatorTrolleyRepository.remove(x));
      }
    }
  }

  private void GenerateHatcherTrolley(Hatchery hatchery) throws AxelorException {
    if(hatchery == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
    }

    List<HatcherTrolley> list =
        hatcherTrolleyRepository.all().filter("self.hatchery = ?1", hatchery).fetch();
    Integer ln =
        list.stream()
            .map(n -> Integer.parseInt(n.getReference() != null ? n.getReference() : "0"))
            .max(Integer::compareTo)
            .orElse(0);

    if (list.stream().count() < hatchery.getNumberOfHatcherTrolleys()) {
      long incToAdd = hatchery.getNumberOfHatcherTrolleys() - list.stream().count();

      for (int i = 0; i < incToAdd; i++) {
        Map<String, Object> values = new HashMap<>();
        values.put("hatchery", hatchery);
        values.put("status", HatcherTrolleyRepository.STATUS_FREE);
        values.put("reference", String.format("%04d", ++ln));
        values.put("capacityOfACartInBaskets", hatchery.getCapacityOfACartInBaskets());
        HatcherTrolley inc = hatcherTrolleyRepository.create(values);
        hatcherTrolleyRepository.save(inc);
      }
    } else {
      long hatToRmv = list.stream().count() - hatchery.getNumberOfHatcherTrolleys();
      if (hatToRmv > 0) {
        List<HatcherTrolley> currentList =
            hatcherTrolleyRepository
                .all()
                .filter(
                    "self.hatchery = ?1 AND self.status != ?2 AND self.status != ?3",
                    hatchery,
                    HatcherTrolleyRepository.STATUS_BUSY,
                    HatcherTrolleyRepository.STATUS_IN_THE_INCUBATOR)
                .fetch((int) hatToRmv);

        if (currentList.stream().count() < hatToRmv) {
          throw new AxelorException(
              hatchery,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHER_TROLLEY_TOBE_REMOVED),
              hatchery.getHatcheryRef());
        }
        currentList.forEach(x -> hatcherTrolleyRepository.remove(x));
      }
    }
  }
  public List<Object[]> fetchRecords() throws AxelorException {
    String sqlQuery = "SELECT hatcher_exit.*, hatcher_entry.*, incubator_exit.*, incubator_entry.*, "
            + "loading_program_line.*, flesh_loading_program_line.* "
            + "FROM pf_hatcher_exit hatcher_exit "
            + "LEFT JOIN pf_hatcher_entry hatcher_entry ON hatcher_exit.hatcher_entry = hatcher_entry.id "
            + "LEFT JOIN pf_incubator_exit incubator_exit ON hatcher_exit.incubator_exit = incubator_exit.id "
            + "LEFT JOIN pf_incubator_entry incubator_entry ON incubator_exit.incubator_entry = incubator_entry.id "
            + "LEFT JOIN pf_incubators_loading_program_line loading_program_line ON incubator_entry.loading_program_line = loading_program_line.id "
            + "LEFT JOIN pf_flesh_loading_program_line flesh_loading_program_line ON hatcher_exit.id = flesh_loading_program_line.hatcher_exit ";

    List<Object[]> result = JPA.em().createNativeQuery(sqlQuery, Object[].class)
            .getResultList();

    System.out.println(result);

    return result;
  }

}


