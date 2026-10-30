package com.gl.apps.livestock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.livestock.db.LivestockAnimal;
import com.gl.apps.livestock.db.LivestockBreedingLog;
import com.gl.apps.livestock.db.LivestockFeedLog;
import com.gl.apps.livestock.db.LivestockHealthLog;
import com.gl.apps.livestock.db.LivestockMilkLog;
import com.gl.apps.livestock.db.LivestockWeightLog;
import java.time.LocalDate;

public interface LivestockOperationsService {

  LivestockHealthLog checkVeterinaryWithdrawal(LivestockAnimal animal, LocalDate logDate);

  StockMove confirmMilkLog(Long milkLogId) throws AxelorException;
  StockMove confirmMilkLog(LivestockMilkLog milkLog) throws AxelorException;

  StockMove confirmFeedLog(Long feedLogId) throws AxelorException;
  StockMove confirmFeedLog(LivestockFeedLog feedLog) throws AxelorException;

  StockMove confirmHealthLog(Long healthLogId) throws AxelorException;
  StockMove confirmHealthLog(LivestockHealthLog healthLog) throws AxelorException;

  LivestockAnimal registerBirth(Long breedingLogId) throws AxelorException;
  LivestockAnimal registerBirth(LivestockBreedingLog breedingLog) throws AxelorException;

  LivestockWeightLog computeWeightGain(LivestockWeightLog weightLog) throws AxelorException;
}
