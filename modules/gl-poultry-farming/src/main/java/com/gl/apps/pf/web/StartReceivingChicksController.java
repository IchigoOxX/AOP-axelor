package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.StartReceivingChicksLineRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ReproProductionService;
import com.gl.apps.pf.service.StartReceivingChicksService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class StartReceivingChicksController {
    @Inject
    private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StartReceivingChicksService startReceivingChicksService;

    public void getLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartReceivingChicks receivingchicks = request.getContext().asType(StartReceivingChicks.class);
        if (receivingchicks != null && receivingchicks.getCenter() != null) {
            receivingchicks.clearReceivingChicksLineList();
            for (BreedingShed x : receivingchicks.getCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_STARTUP && x.getStatus() == BreedingShedRepository.STATUS_ACTIVATED) {
                    StartReceivingChicksLine rline = new StartReceivingChicksLine();
                    rline.setBreedingShed(x);
                    receivingchicks.addReceivingChicksLineListItem(rline);
                }
            }
        }
        response.setValues(receivingchicks);
    }

    @Transactional
    public void getValuesFromSupplierReception(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartReceivingChicks receivingchicks = request.getContext().asType(StartReceivingChicks.class);
        if (receivingchicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIVING_CHECKS_NOT_FOUND));
        }
        if (receivingchicks.getSupplierReception() == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.SUPLLIER_RECEPTION_NOT_FOUND));
        }

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        int tFemales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getRealQty().intValue()).sum():0;

        int tMales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getRealQty().intValue()).sum():0;

        int invFemales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getQtyInvoiced().intValue()).sum():0;
        int invMales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getQtyInvoiced().intValue()).sum():0;


        int femaleUnitPrice = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getUnitPriceUntaxed().intValue()).sum():0;
        int malesUnitPrice = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getUnitPriceUntaxed().intValue()).sum():0;

        int costPrice = (tFemales * femaleUnitPrice) + (tMales * malesUnitPrice);
        int amount = (invFemales * femaleUnitPrice) + (invMales * malesUnitPrice);

        receivingchicks.setReceivedFemalesNumber(tFemales);
        receivingchicks.setReceivedMalesNumber(tMales);
        receivingchicks.setReceivedChicksNumber(tFemales+ tMales);
        receivingchicks.setInvoicedFemalesNumber(invFemales);
        receivingchicks.setInvoicedMalesNumber(invMales);
        receivingchicks.setInvoicedChicksNumber(invFemales + invMales);

        receivingchicks.setCostPrice(costPrice);
        receivingchicks.setAmount(amount);

        //response.setReload(true);
        response.setValues(receivingchicks);

    }

    @Transactional
    public void refreshValuesFromSupplierReception(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartReceivingChicks receivingchicks = request.getContext().asType(StartReceivingChicks.class);
        if (receivingchicks == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIVING_CHECKS_NOT_FOUND));
        }
        if (receivingchicks.getSupplierReception() == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.SUPLLIER_RECEPTION_NOT_FOUND));
        }

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        int tFemales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getRealQty().intValue()).sum():0;

        int tMales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getRealQty().intValue()).sum():0;

        int invFemales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getQtyInvoiced().intValue()).sum():0;
        int invMales = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getQtyInvoiced().intValue()).sum():0;


        int femaleUnitPrice = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterFemalesChicks())
                .mapToInt(t-> t.getUnitPriceUntaxed().intValue()).sum():0;
        int malesUnitPrice = receivingchicks.getSupplierReception()
                .getStockMoveLineList() != null ? receivingchicks.getSupplierReception()
                .getStockMoveLineList().stream().filter(t->t.getProduct()== app.getStarterMalesChicks())
                .mapToInt(t-> t.getUnitPriceUntaxed().intValue()).sum():0;

        int amount = (invFemales * femaleUnitPrice) + (invMales * malesUnitPrice);

        receivingchicks.setReceivedChicksNumber(tFemales+ tMales);
        receivingchicks.setInvoicedFemalesNumber(invFemales);
        receivingchicks.setInvoicedMalesNumber(invMales);
        receivingchicks.setInvoicedChicksNumber(invFemales + invMales);

        receivingchicks.setAmount(amount);

        //response.setReload(true);
        response.setValues(receivingchicks);

    }

    @Transactional
    public void startChicksValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartReceivingChicks startReceivingChicks = request.getContext().asType(StartReceivingChicks.class);

        if (startReceivingChicks != null ) {
            startReceivingChicksService.Validate(startReceivingChicks);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIVING_CHECKS_NOT_FOUND));
        }

        response.setValues(startReceivingChicks);
        response.setReload(true);
    }

    @Transactional
    public void startChicksCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        StartReceivingChicks startReceivingChicks = request.getContext().asType(StartReceivingChicks.class);

        if (startReceivingChicks != null ) {
            startReceivingChicksService.Cancel(startReceivingChicks);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIVING_CHECKS_NOT_FOUND));
        }

        response.setValues(startReceivingChicks);
        response.setReload(true);
    }

}
