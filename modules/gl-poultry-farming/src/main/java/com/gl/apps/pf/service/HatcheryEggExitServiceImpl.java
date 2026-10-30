package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.HatcheryEggExit;
import com.gl.apps.pf.db.HatcheryEggExitLine;
import com.gl.apps.pf.db.HatcheryEggExitLineDetail;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HatcheryEggExitServiceImpl implements HatcheryEggExitService{
    @Inject
    private HatcheryEggExitRepository hatcheryEggExitRepository;
    @Inject
    private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public HatcheryEggExit Validate(HatcheryEggExit hatcheryEggExit) throws AxelorException {

        HatcheryEggExit hatcheryEggExit1 = hatcheryEggExitRepository.find(hatcheryEggExit.getId());

        if (hatcheryEggExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_FOUND));
        }
        StockMove stockMove = null;

        StockMove dispatcherMove = CreateOrUpdateStockMove(hatcheryEggExit1);
        for (StockMoveLine line : dispatcherMove.getStockMoveLineList()) {
            BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(dispatcherMove.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                    if (!line.getProduct().getAllowNegativeStock()) {
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + dispatcherMove.getFromStockLocation().getName() + " !")
                            );
                        }
                    }
        }
        hatcheryEggExit1.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_HATCHERY_EGGS_EXIT);
        dispatcherMove.setRealDate(LocalDate.now());
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);

        hatcheryEggExit1.setStatus(HatcheryEggExitRepository.STATUS_EXECUTING);
        return hatcheryEggExitRepository.save(hatcheryEggExit1);
    }

    @Transactional
    public HatcheryEggExit Cancel(HatcheryEggExit hatcheryEggExit) throws AxelorException {

        HatcheryEggExit hatcheryEggExit1 = hatcheryEggExitRepository.find(hatcheryEggExit.getId());

        if (hatcheryEggExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_FOUND));
        }

        hatcheryEggExit1.setReasonOfCancel(hatcheryEggExit1.getReasonOfCancel());
        hatcheryEggExit1.setStatus(HatcherEntryRepository.STATUS_CANCELED);

        StockMove stockMove = hatcheryEggExit1.getDispatchStock();
        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }
        return hatcheryEggExitRepository.save(hatcheryEggExit1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(HatcheryEggExit hatcheryEggExit)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (hatcheryEggExit == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_FOUND));
        }

        HatcheryEggExit hatcheryEggExit1 = hatcheryEggExitRepository.find(hatcheryEggExit.getId());

        if (hatcheryEggExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_SPECIFIED));
        }

        Product OACEggs = app.getHatchingEggProduct();
        StockLocation virtualHatcheryLocation = app.getVirtualHatcheryLocation();
        StockLocation  virtualVariousOutingsLocation = app.getVirtualVariousOutingsLocation();

        StockMove stockMove;
        if (hatcheryEggExit1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(hatcheryEggExit1.getHatchery().getUnit().getCompany() != null ){
                values.put("company", hatcheryEggExit1.getHatchery().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
            }
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            values.put("note", hatcheryEggExit1.getReason());
            if(virtualHatcheryLocation != null ){
                values.put("fromStockLocation", virtualHatcheryLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
            }
            if(virtualVariousOutingsLocation != null ){
                values.put("toStockLocation", virtualVariousOutingsLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VARIOUS_OUTING_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setHatcheryEggExit(hatcheryEggExit);
        }
        else {
            stockMove = hatcheryEggExit1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!hatcheryEggExit1.getHatcheryEggExitList().isEmpty() ) {
            for (HatcheryEggExitLine line : hatcheryEggExit1.getHatcheryEggExitList()) {
                if (line != null) {
                    for (HatcheryEggExitLineDetail lineDetails : line.getHatcheryEggExitLineDetail()) {
                        StockMoveLine sm = new StockMoveLine();
                        if (lineDetails.getNumberOfExitEggs() != 0 || lineDetails.getNumberOfExitEggTrays() != 0) {
                            if (OACEggs != null) {
                                sm.setProduct(OACEggs);
                            } else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.OAC_EGGS_NOT_SPECIFIED));
                            }

                            sm.setQty(BigDecimal.valueOf(lineDetails.getNumberOfExitEggs()));
                            sm.setRealQty(BigDecimal.valueOf(lineDetails.getNumberOfExitEggs()));
                            if(virtualHatcheryLocation != null) {
                                sm.setFromStockLocation(virtualHatcheryLocation);
                            }
                            else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                            }
                            if(virtualVariousOutingsLocation != null ){
                                sm.setToStockLocation(virtualVariousOutingsLocation);
                            }
                            else {
                                throw new AxelorException(
                                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                                        I18n.get(PoultryFarmingExceptionMessage.VARIOUS_OUTING_LOCATION_NOT_SPECIFIED));
                            }
                            sm.setProductName(sm.getProduct().getName());
                            sm.setUnit(sm.getProduct().getUnit());
                            stockMove.addStockMoveLineListItem(sm);
                        }
                    }
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public HatcheryEggExit Revalidate(HatcheryEggExit hatcheryEggExit) throws AxelorException {
        HatcheryEggExit hatcheryEggExit1 = hatcheryEggExitRepository.find(hatcheryEggExit.getId());

        if (hatcheryEggExit1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_EXIT_NOT_FOUND));
        }
        StockMove stockMove = hatcheryEggExit1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.goBackToDraft(stockMove);
        }
        hatcheryEggExit1.setStatus(HatcheryEggExitRepository.STATUS_NOT_EXECUTED);

        return hatcheryEggExitRepository.save(hatcheryEggExit1);
    }
}




