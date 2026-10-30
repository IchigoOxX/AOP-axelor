package com.gl.apps.livestock.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.livestock.db.LivestockAnimal;
import com.gl.apps.livestock.db.LivestockBreedingLog;
import com.gl.apps.livestock.db.LivestockFeedLog;
import com.gl.apps.livestock.db.LivestockHealthLog;
import com.gl.apps.livestock.db.LivestockMilkLog;
import com.gl.apps.livestock.db.LivestockWeightLog;
import com.gl.apps.livestock.db.repo.LivestockBreedingLogRepository;
import com.gl.apps.livestock.db.repo.LivestockFeedLogRepository;
import com.gl.apps.livestock.db.repo.LivestockHealthLogRepository;
import com.gl.apps.livestock.db.repo.LivestockMilkLogRepository;
import com.gl.apps.livestock.db.repo.LivestockWeightLogRepository;
import com.gl.apps.livestock.service.LivestockOperationsService;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LivestockOperationsController {

  /**
   * Confirm Milk Log, check veterinary withdrawal, and generate inward StockMove to Cooling Tank.
   */
  public void confirmMilkLog(ActionRequest request, ActionResponse response) {
    try {
      LivestockMilkLog log = request.getContext().asType(LivestockMilkLog.class);
      if (log.getId() != null) {
        log = Beans.get(LivestockMilkLogRepository.class).find(log.getId());
      }
      if (log == null) {
        response.setError("Milk log record not found.");
        return;
      }

      // 1. Veterinary Withdrawal Check
      LivestockOperationsService service = Beans.get(LivestockOperationsService.class);
      LivestockHealthLog activeWithdrawal = service.checkVeterinaryWithdrawal(log.getAnimal(), log.getLogDate());
      if (activeWithdrawal != null) {
        response.setError(
            "Cannot send milk to cooling tank: Animal is under active veterinary withdrawal period until "
                + activeWithdrawal.getWithdrawalEndDate());
        return;
      }

      // 2. Generate and realize inward StockMove
      StockMove move = service.confirmMilkLog(log);
      response.setValue("stockMove", move);
      response.setValue("statusSelect", 1);
      response.setValues(log);
      response.setReload(true);
      response.setNotify("Milk Log confirmed successfully. Stock Move generated: " + (move.getName() != null ? move.getName() : move.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Calculate Feed Log totalCost on the fly when quantity or unit cost changes.
   */
  public void onFeedCostChange(ActionRequest request, ActionResponse response) {
    try {
      LivestockFeedLog log = request.getContext().asType(LivestockFeedLog.class);
      if (log.getQuantityKg() != null && log.getUnitCost() != null) {
        BigDecimal total = log.getQuantityKg().multiply(log.getUnitCost());
        response.setValue("totalCost", total);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Confirm Feed Log, calculate total cost, and generate outward/internal StockMove from warehouse to barn.
   */
  public void confirmFeedLog(ActionRequest request, ActionResponse response) {
    try {
      LivestockFeedLog log = request.getContext().asType(LivestockFeedLog.class);
      if (log.getId() != null) {
        log = Beans.get(LivestockFeedLogRepository.class).find(log.getId());
      }
      if (log == null) {
        response.setError("Feed log record not found.");
        return;
      }

      LivestockOperationsService service = Beans.get(LivestockOperationsService.class);
      StockMove move = service.confirmFeedLog(log);
      response.setValue("totalCost", log.getTotalCost());
      response.setValue("stockMove", move);
      response.setValue("statusSelect", 1);
      response.setValues(log);
      response.setReload(true);
      response.setNotify("Feed Log confirmed. Stock Move generated: " + (move.getName() != null ? move.getName() : move.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Confirm Health Log, deduct medicine quantity from pharmacy to animal's barn via StockMove.
   */
  public void confirmHealthLog(ActionRequest request, ActionResponse response) {
    try {
      LivestockHealthLog log = request.getContext().asType(LivestockHealthLog.class);
      if (log.getId() != null) {
        log = Beans.get(LivestockHealthLogRepository.class).find(log.getId());
      }
      if (log == null) {
        response.setError("Health log record not found.");
        return;
      }

      LivestockOperationsService service = Beans.get(LivestockOperationsService.class);
      StockMove move = service.confirmHealthLog(log);
      response.setValue("stockMove", move);
      response.setValue("statusSelect", 1);
      response.setValues(log);
      response.setReload(true);
      response.setNotify("Health Log confirmed. Medicine Stock Move generated: " + (move.getName() != null ? move.getName() : move.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * On Insemination or Pregnancy change, auto-calculate expectedDryOffDate (+220d) and expectedCalvingDate (+280d).
   */
  public void onInseminationChange(ActionRequest request, ActionResponse response) {
    try {
      LivestockBreedingLog log = request.getContext().asType(LivestockBreedingLog.class);
      if (Boolean.TRUE.equals(log.getIsPregnant()) && log.getInseminationDate() != null) {
        LocalDate dryOff = log.getInseminationDate().plusDays(220);
        LocalDate calving = log.getInseminationDate().plusDays(280);
        response.setValue("expectedDryOffDate", dryOff);
        response.setValue("expectedCalvingDate", calving);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Register Birth of new calf from Breeding Log.
   */
  public void registerBirth(ActionRequest request, ActionResponse response) {
    try {
      LivestockBreedingLog log = request.getContext().asType(LivestockBreedingLog.class);
      if (log.getId() != null) {
        log = Beans.get(LivestockBreedingLogRepository.class).find(log.getId());
      }
      if (log == null) {
        response.setError("Breeding log record not found.");
        return;
      }

      if (log.getActualCalvingDate() == null) {
        response.setError("Please enter Actual Calving Date.");
        return;
      }
      if (log.getCalfEarTag() == null || log.getCalfEarTag().trim().isEmpty()) {
        response.setError("Please enter Calf Ear Tag.");
        return;
      }

      LivestockOperationsService service = Beans.get(LivestockOperationsService.class);
      LivestockAnimal calf = service.registerBirth(log);
      response.setValue("newbornCalf", calf);
      response.setValues(log);
      response.setReload(true);
      response.setNotify("Newborn calf birth registered successfully: " + calf.getEarTag());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Calculate Average Daily Gain (ADG) and update Animal current weight / last weigh date.
   */
  public void computeWeightGain(ActionRequest request, ActionResponse response) {
    try {
      LivestockWeightLog log = request.getContext().asType(LivestockWeightLog.class);
      if (log.getId() != null) {
        log = Beans.get(LivestockWeightLogRepository.class).find(log.getId());
      }
      if (log == null || log.getAnimal() == null || log.getWeight() == null || log.getWeighDate() == null) {
        response.setError("Animal, Weigh Date, and Weight are required to calculate ADG.");
        return;
      }

      LivestockOperationsService service = Beans.get(LivestockOperationsService.class);
      log = service.computeWeightGain(log);
      response.setValue("daysSinceLastWeigh", log.getDaysSinceLastWeigh());
      response.setValue("dailyGainKg", log.getDailyGainKg());
      response.setValues(log);
      response.setNotify("ADG calculated: " + log.getDailyGainKg() + " kg/day (" + log.getDaysSinceLastWeigh() + " days)");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
