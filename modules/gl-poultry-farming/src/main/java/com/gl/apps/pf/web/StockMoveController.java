package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.StartReceivingChicks;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.StartReceivingChicksService;
import com.gl.apps.pf.service.StockMoveService;
import com.google.inject.Inject;
import org.eclipse.birt.chart.extension.render.Stock;

import com.google.inject.persist.Transactional;
import java.util.List;

public class StockMoveController {
    @Inject
    private StockMoveService stockMoveservice;
    @Inject private StockMoveRepository stockMoveRepository;

    @Transactional
    public void updateValuesForLine(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StockMove stockMouv = request.getContext().asType(StockMove.class);

        if (stockMouv != null ) {
            stockMoveservice.SetValuesLineStockMove(stockMouv);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND));
        }

        response.setValues(stockMouv);
        response.setReload(true);
    }
    @Transactional
    public void SaveTest(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StockMove stockMouv = request.getContext().asType(StockMove.class);

        if (stockMouv != null ) {
            stockMoveservice.saveTest(request.getContext());
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND));
        }

        response.setValues(stockMouv);
        //response.setReload(true);
    }
    @Transactional
    public void addChicksReceptionStartUp(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StockMove stockMouv = request.getContext().asType(StockMove.class);

        if (stockMouv != null ) {
            stockMoveservice.addChicksReception(stockMouv);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND));
        }

        response.setValues(stockMouv);
        response.setReload(true);
    }



}

