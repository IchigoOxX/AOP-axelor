package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.TechnicalSheet;
import com.gl.apps.pf.db.TechnicalSheetLine;

public interface TechnicalSheetService {
    public TechnicalSheet Validate(TechnicalSheet technicalSheet) throws AxelorException;
    public TechnicalSheetLine ValidateLine(TechnicalSheetLine technicalSheetLine) throws AxelorException;
    public TechnicalSheetLine test(TechnicalSheetLine technicalSheetLine) throws AxelorException;
   // public TechnicalSheetLine getDatePreviousTechnicalSheet(TechnicalSheetLine technicalSheetLine) throws AxelorException;
   public StockMove CreateOrUpdateStockMove(TechnicalSheetLine technicalSheetLine) throws AxelorException;
}
