package com.gl.apps.livestock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.gl.apps.livestock.db.LivestockAnimal;
import com.gl.apps.livestock.db.LivestockBreedingLog;
import com.gl.apps.livestock.db.LivestockFeedLog;
import com.gl.apps.livestock.db.LivestockHealthLog;
import com.gl.apps.livestock.db.LivestockMilkLog;
import com.gl.apps.livestock.db.LivestockWeightLog;
import com.gl.apps.livestock.db.repo.LivestockAnimalRepository;
import com.gl.apps.livestock.db.repo.LivestockBreedingLogRepository;
import com.gl.apps.livestock.db.repo.LivestockFeedLogRepository;
import com.gl.apps.livestock.db.repo.LivestockHealthLogRepository;
import com.gl.apps.livestock.db.repo.LivestockMilkLogRepository;
import com.gl.apps.livestock.db.repo.LivestockWeightLogRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LivestockOperationsServiceImpl implements LivestockOperationsService {

  protected final LivestockMilkLogRepository milkLogRepo;
  protected final LivestockFeedLogRepository feedLogRepo;
  protected final LivestockHealthLogRepository healthLogRepo;
  protected final LivestockBreedingLogRepository breedingLogRepo;
  protected final LivestockWeightLogRepository weightLogRepo;
  protected final LivestockAnimalRepository animalRepo;
  protected final StockMoveRepository stockMoveRepo;
  protected final StockMoveService stockMoveService;

  @Inject
  public LivestockOperationsServiceImpl(
      LivestockMilkLogRepository milkLogRepo,
      LivestockFeedLogRepository feedLogRepo,
      LivestockHealthLogRepository healthLogRepo,
      LivestockBreedingLogRepository breedingLogRepo,
      LivestockWeightLogRepository weightLogRepo,
      LivestockAnimalRepository animalRepo,
      StockMoveRepository stockMoveRepo,
      StockMoveService stockMoveService) {
    this.milkLogRepo = milkLogRepo;
    this.feedLogRepo = feedLogRepo;
    this.healthLogRepo = healthLogRepo;
    this.breedingLogRepo = breedingLogRepo;
    this.weightLogRepo = weightLogRepo;
    this.animalRepo = animalRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.stockMoveService = stockMoveService;
  }

  @Override
  public LivestockHealthLog checkVeterinaryWithdrawal(LivestockAnimal animal, LocalDate logDate) {
    if (animal == null || logDate == null) {
      return null;
    }
    return healthLogRepo.all()
        .filter("self.animal = :animal AND self.withdrawalEndDate >= :logDate")
        .bind("animal", animal)
        .bind("logDate", logDate)
        .order("-withdrawalEndDate")
        .fetchOne();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmMilkLog(Long milkLogId) throws AxelorException {
    LivestockMilkLog log = milkLogRepo.find(milkLogId);
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Milk Log not found.");
    }
    return confirmMilkLog(log);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmMilkLog(LivestockMilkLog log) throws AxelorException {
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Milk Log cannot be null.");
    }

    // 1. Veterinary Withdrawal Check
    LivestockHealthLog activeWithdrawal = checkVeterinaryWithdrawal(log.getAnimal(), log.getLogDate());
    if (activeWithdrawal != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "Cannot send milk to cooling tank: Animal is under active veterinary withdrawal period until "
              + activeWithdrawal.getWithdrawalEndDate());
    }

    // 2. Validations
    if (log.getMilkProduct() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Raw Milk Product is required.");
    }
    if (log.getStorageTank() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Cooling Tank (Stock Location) is required.");
    }
    if (log.getQuantityLiters() == null || log.getQuantityLiters().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Quantity in liters must be greater than zero.");
    }

    // 3. Generate Inward StockMove
    StockMove move = new StockMove();
    move.setCompany(log.getCompany());
    move.setTypeSelect(StockMoveRepository.TYPE_INCOMING);
    move.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
    move.setToStockLocation(log.getStorageTank());
    LocalDate date = log.getLogDate() != null ? log.getLogDate() : LocalDate.now();
    move.setRealDate(date);
    move.setEstimatedDate(date);
    String animalTag = log.getAnimal() != null ? log.getAnimal().getEarTag() : "Bulk Herd";
    move.setNote("Milk Production Log - Animal: " + animalTag + " - Tank: " + log.getStorageTank().getName());

    StockMoveLine line = new StockMoveLine();
    line.setProduct(log.getMilkProduct());
    line.setProductName(log.getMilkProduct().getName());
    line.setUnit(log.getMilkProduct().getUnit());
    line.setQty(log.getQuantityLiters());
    line.setRealQty(log.getQuantityLiters());
    line.setToStockLocation(log.getStorageTank());
    move.addStockMoveLineListItem(line);

    move = stockMoveRepo.save(move);
    stockMoveService.plan(move);
    stockMoveService.realize(move, true);

    log.setStockMove(move);
    log.setStatusSelect(1); // Confirmed
    milkLogRepo.save(log);

    return move;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmFeedLog(Long feedLogId) throws AxelorException {
    LivestockFeedLog log = feedLogRepo.find(feedLogId);
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Feed Log not found.");
    }
    return confirmFeedLog(log);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmFeedLog(LivestockFeedLog log) throws AxelorException {
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Feed Log cannot be null.");
    }

    // 1. Auto-calculate totalCost
    if (log.getQuantityKg() != null && log.getUnitCost() != null) {
      log.setTotalCost(log.getQuantityKg().multiply(log.getUnitCost()));
    }

    // 2. Validations
    if (log.getFeedProduct() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Feed Product is required.");
    }
    if (log.getFromLocation() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Feed Warehouse (From Location) is required.");
    }
    if (log.getLocation() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Barn / Pen Location is required.");
    }
    if (log.getQuantityKg() == null || log.getQuantityKg().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Quantity in Kg must be greater than zero.");
    }

    // 3. Generate internal StockMove from feed warehouse to barn location
    StockMove move = new StockMove();
    move.setCompany(log.getCompany());
    move.setTypeSelect(StockMoveRepository.TYPE_INTERNAL);
    move.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
    move.setFromStockLocation(log.getFromLocation());
    move.setToStockLocation(log.getLocation());
    LocalDate date = log.getFeedDate() != null ? log.getFeedDate() : LocalDate.now();
    move.setRealDate(date);
    move.setEstimatedDate(date);
    String unitName = log.getUnit() != null ? log.getUnit().getName() : "";
    move.setNote("Feed Distribution - Barn: " + log.getLocation().getName() + (unitName.isEmpty() ? "" : " - Cost Center: " + unitName));

    StockMoveLine line = new StockMoveLine();
    line.setProduct(log.getFeedProduct());
    line.setProductName(log.getFeedProduct().getName());
    line.setUnit(log.getFeedProduct().getUnit());
    line.setQty(log.getQuantityKg());
    line.setRealQty(log.getQuantityKg());
    if (log.getUnitCost() != null) {
      line.setUnitPriceUntaxed(log.getUnitCost());
    }
    line.setFromStockLocation(log.getFromLocation());
    line.setToStockLocation(log.getLocation());
    move.addStockMoveLineListItem(line);

    move = stockMoveRepo.save(move);
    stockMoveService.plan(move);
    stockMoveService.realize(move, true);

    log.setStockMove(move);
    log.setStatusSelect(1); // Confirmed
    feedLogRepo.save(log);

    return move;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmHealthLog(Long healthLogId) throws AxelorException {
    LivestockHealthLog log = healthLogRepo.find(healthLogId);
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Health Log not found.");
    }
    return confirmHealthLog(log);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove confirmHealthLog(LivestockHealthLog log) throws AxelorException {
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Health Log cannot be null.");
    }

    // Validations
    if (log.getMedicineProduct() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Medicine / Vaccine Product is required.");
    }
    if (log.getPharmacyLocation() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Pharmacy Location is required.");
    }
    if (log.getQuantityUsed() == null || log.getQuantityUsed().compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Quantity Used must be greater than zero.");
    }

    StockLocation barnLocation = log.getAnimal() != null ? log.getAnimal().getLocation() : null;

    StockMove move = new StockMove();
    move.setCompany(log.getCompany() != null ? log.getCompany() : (log.getAnimal() != null ? log.getAnimal().getCompany() : null));
    move.setTypeSelect(barnLocation != null ? StockMoveRepository.TYPE_INTERNAL : StockMoveRepository.TYPE_OUTGOING);
    move.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
    move.setFromStockLocation(log.getPharmacyLocation());
    if (barnLocation != null) {
      move.setToStockLocation(barnLocation);
    }
    LocalDate date = log.getEventDate() != null ? log.getEventDate() : LocalDate.now();
    move.setRealDate(date);
    move.setEstimatedDate(date);
    String animalTag = log.getAnimal() != null ? log.getAnimal().getEarTag() : "";
    move.setNote("Veterinary Treatment - Animal: " + animalTag + (log.getDiagnosis() != null ? " - Diagnosis: " + log.getDiagnosis() : ""));

    StockMoveLine line = new StockMoveLine();
    line.setProduct(log.getMedicineProduct());
    line.setProductName(log.getMedicineProduct().getName());
    line.setUnit(log.getMedicineProduct().getUnit());
    line.setQty(log.getQuantityUsed());
    line.setRealQty(log.getQuantityUsed());
    line.setFromStockLocation(log.getPharmacyLocation());
    if (barnLocation != null) {
      line.setToStockLocation(barnLocation);
    }
    move.addStockMoveLineListItem(line);

    move = stockMoveRepo.save(move);
    stockMoveService.plan(move);
    stockMoveService.realize(move, true);

    log.setStockMove(move);
    log.setStatusSelect(1); // Confirmed
    healthLogRepo.save(log);

    return move;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LivestockAnimal registerBirth(Long breedingLogId) throws AxelorException {
    LivestockBreedingLog log = breedingLogRepo.find(breedingLogId);
    if (log == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Breeding Log not found.");
    }
    return registerBirth(log);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LivestockAnimal registerBirth(LivestockBreedingLog log) throws AxelorException {
    if (log.getActualCalvingDate() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Actual Calving Date is required.");
    }
    if (log.getCalfEarTag() == null || log.getCalfEarTag().trim().isEmpty()) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Calf Ear Tag is required.");
    }

    String calfTag = log.getCalfEarTag().trim();
    LivestockAnimal existing = animalRepo.all()
        .filter("self.earTag = :tag")
        .bind("tag", calfTag)
        .fetchOne();
    if (existing != null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, "Animal with Ear Tag " + calfTag + " already exists!");
    }

    LivestockAnimal dam = log.getAnimal();
    LivestockAnimal calf = new LivestockAnimal();
    calf.setEarTag(calfTag);
    calf.setName("Calf " + calfTag);
    calf.setGenderSelect(log.getCalfGenderSelect() != null ? log.getCalfGenderSelect() : 2);
    calf.setBirthDate(log.getActualCalvingDate());
    calf.setEntryDate(log.getActualCalvingDate());
    calf.setSpeciesSelect(dam != null ? dam.getSpeciesSelect() : 1);
    calf.setDam(dam);
    calf.setDamTag(dam != null ? dam.getEarTag() : null);
    calf.setCompany(log.getCompany() != null ? log.getCompany() : (dam != null ? dam.getCompany() : null));
    calf.setUnit(dam != null ? dam.getUnit() : null);
    calf.setLocation(dam != null ? dam.getLocation() : null);
    calf.setStatusSelect(2); // Active Feedlot / Calves

    calf = animalRepo.save(calf);

    log.setNewbornCalf(calf);
    breedingLogRepo.save(log);

    return calf;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public LivestockWeightLog computeWeightGain(LivestockWeightLog log) throws AxelorException {
    if (log == null || log.getAnimal() == null || log.getWeight() == null || log.getWeighDate() == null) {
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, "Animal, Weigh Date, and Weight are required.");
    }

    BigDecimal prevWeight = null;
    LocalDate prevDate = null;

    LivestockWeightLog prevLog = weightLogRepo.all()
        .filter("self.animal = :animal AND self.weighDate < :weighDate")
        .bind("animal", log.getAnimal())
        .bind("weighDate", log.getWeighDate())
        .order("-weighDate")
        .fetchOne();

    if (prevLog != null) {
      prevWeight = prevLog.getWeight();
      prevDate = prevLog.getWeighDate();
    } else {
      LivestockAnimal animal = animalRepo.find(log.getAnimal().getId());
      if (animal != null) {
        if (animal.getLastWeighDate() != null && animal.getLastWeighDate().isBefore(log.getWeighDate()) && animal.getCurrentWeight() != null) {
          prevWeight = animal.getCurrentWeight();
          prevDate = animal.getLastWeighDate();
        } else if (animal.getBirthDate() != null && animal.getBirthWeight() != null && !animal.getBirthDate().isAfter(log.getWeighDate())) {
          prevWeight = animal.getBirthWeight();
          prevDate = animal.getBirthDate();
        }
      }
    }

    if (prevWeight != null && prevDate != null) {
      long days = ChronoUnit.DAYS.between(prevDate, log.getWeighDate());
      if (days > 0) {
        log.setDaysSinceLastWeigh((int) days);
        BigDecimal weightDiff = log.getWeight().subtract(prevWeight);
        BigDecimal dailyGain = weightDiff.divide(BigDecimal.valueOf(days), 3, RoundingMode.HALF_UP);
        log.setDailyGainKg(dailyGain);
      } else {
        log.setDaysSinceLastWeigh(0);
        log.setDailyGainKg(BigDecimal.ZERO);
      }
    }

    // Update animal's current weight and last weigh date
    if (log.getAnimal() != null && log.getAnimal().getId() != null) {
      LivestockAnimal animal = animalRepo.find(log.getAnimal().getId());
      if (animal != null) {
        animal.setCurrentWeight(log.getWeight());
        animal.setLastWeighDate(log.getWeighDate());
        animalRepo.save(animal);
      }
    }

    if (log.getId() != null) {
      log = weightLogRepo.save(log);
    }
    return log;
  }
}
