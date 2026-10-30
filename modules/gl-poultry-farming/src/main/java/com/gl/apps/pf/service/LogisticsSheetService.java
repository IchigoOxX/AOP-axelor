package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import com.gl.apps.pf.db.LogisticsSheet;
import com.gl.apps.pf.db.ProductConsumption;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface LogisticsSheetService {
    public LogisticsSheet Validate(LogisticsSheet logisticsSheet) throws AxelorException;
    public LogisticsSheet Realize(LogisticsSheet logisticsSheet) throws AxelorException;

}
