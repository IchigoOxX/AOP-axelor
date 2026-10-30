package com.gl.apps.pf.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.exception.StockExceptionMessage;
/* import com.axelor.apps.stock.report.IReport; */
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
/* import com.axelor.utils.ModelTool; */
import com.axelor.utils.ThrowConsumer;
/* import com.axelor.utils.file.PdfTool; */
import com.gl.apps.pf.db.LogisticsSheet;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.repo.LogisticsSheetRepository;
import com.gl.apps.pf.db.repo.PharmacyTreatmentRepository;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LogisticsSheetServiceImpl implements LogisticsSheetService {
    protected AppBaseService appBaseService;
    @Inject private LogisticsSheetRepository logisticsSheetRepository;
    @Transactional
    public LogisticsSheet Validate(LogisticsSheet logisticsSheet) throws AxelorException {
        LogisticsSheet logSheet = logisticsSheetRepository.find(logisticsSheet.getId());
        if (logSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LOGISTICS_SHEET_NOT_FOUND));
        }

        logSheet.setStatus(LogisticsSheetRepository.STATUS_EXECUTING);
        return logisticsSheetRepository.save(logSheet);

    }

    @Transactional
    public LogisticsSheet Realize(LogisticsSheet logisticsSheet) throws AxelorException {
        LogisticsSheet logSheet = logisticsSheetRepository.find(logisticsSheet.getId());
        if (logSheet == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LOGISTICS_SHEET_NOT_FOUND));
        }

        logSheet.setStatus(LogisticsSheetRepository.STATUS_EXECUTED);
        return logisticsSheetRepository.save(logSheet);

    }

}

