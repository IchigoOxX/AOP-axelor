package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
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
import com.gl.apps.pf.db.HatcheryDeliverySale;
import com.gl.apps.pf.db.HatcheryDeliverySaleLine;
import com.gl.apps.pf.db.HatcheryEggExit;
import com.gl.apps.pf.db.repo.ElevReformRepository;
import com.gl.apps.pf.db.repo.HatcherEntryRepository;
import com.gl.apps.pf.db.repo.HatcheryDeliverySaleRepository;
import com.gl.apps.pf.db.repo.HatcheryEggExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class HatcheryDeliverySaleServiceImpl implements  HatcheryDeliverySaleService {
    @Inject private HatcheryDeliverySaleRepository hatcheryDeliverySaleRepository;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockLocationUtilsService stockLocationService;

    @Transactional
    public HatcheryDeliverySale Validate(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException {

        HatcheryDeliverySale hatcheryDeliverySale1 = hatcheryDeliverySaleRepository.find(hatcheryDeliverySale.getId());

        if (hatcheryDeliverySale1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(hatcheryDeliverySale1);
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
        hatcheryDeliverySale1.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_HATCHERY_DELIVERY_SALE);
        dispatcherMove.setRealDate(LocalDate.now());
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);

        hatcheryDeliverySale1.setStatus(ElevReformRepository.STATUS_EXECUTING);
        hatcheryDeliverySale1.getCustomerSaleOrder().setStatusSelect(SaleOrderRepository.STATUS_ORDER_COMPLETED);

        return hatcheryDeliverySaleRepository.save(hatcheryDeliverySale1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(HatcheryDeliverySale hatcheryDeliverySale)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (hatcheryDeliverySale == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_DELIVERY_SALE_EXIT_NOT_FOUND));
        }

        HatcheryDeliverySale hatcheryDeliverySale1 = hatcheryDeliverySaleRepository.find(hatcheryDeliverySale.getId());

        if (hatcheryDeliverySale1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_DELIVERY_SALE_NOT_SPECIFIED));
        }

        StockLocation virtualHatcheryLocation = app.getVirtualHatcheryLocation();
        StockLocation physicalSaleLocation = app.getPhysicalSaleLocation();

        StockMove stockMove;

        if (hatcheryDeliverySale1.getDispatchStock() == null) {

            Map<String, Object> values = new HashMap<>();
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if (hatcheryDeliverySale1.getHatchery().getUnit().getCompany() == null) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_NOT_FOUND));
            }
            if(hatcheryDeliverySale1.getHatchery().getUnit().getCompany() != null){
                values.put("company", hatcheryDeliverySale1.getHatchery().getUnit().getCompany());
            }
            else throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
            if(virtualHatcheryLocation != null){
                values.put("fromStockLocation", virtualHatcheryLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
            }
           if(physicalSaleLocation != null){
               values.put("toStockLocation", physicalSaleLocation);
           }
           else {
               throw new AxelorException(
                       TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                       I18n.get(PoultryFarmingExceptionMessage.PHYSICAL_SALE_LOCATION_NOT_SPECIFIED));
           }

            stockMove = stockMoveRepo.create(values);
            stockMove.setHatcheryDeliverySale(hatcheryDeliverySale1);
        }
        else {
            stockMove = hatcheryDeliverySale1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!hatcheryDeliverySale1.getHatcheryDeliverySaleList().isEmpty() ) {
            for (HatcheryDeliverySaleLine line : hatcheryDeliverySale1.getHatcheryDeliverySaleList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    sm.setProduct(line.getProduct());
                    sm.setQty(line.getReelDeliveredQty());
                    sm.setRealQty(line.getReelDeliveredQty());
                    if (virtualHatcheryLocation != null) {
                        sm.setFromStockLocation(virtualHatcheryLocation);
                    } else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                                I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                    }
                    if(physicalSaleLocation != null) {
                        sm.setToStockLocation(physicalSaleLocation);
                    }
                    else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                                I18n.get(PoultryFarmingExceptionMessage.PHYSICAL_SALE_LOCATION_NOT_SPECIFIED));
                    }
                    sm.setProductName(sm.getProduct().getName());
                    sm.setUnit(sm.getProduct().getUnit());
                    stockMove.addStockMoveLineListItem(sm);
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public HatcheryDeliverySale Cancel(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException {

        HatcheryDeliverySale hatcheryDeliverySale1 = hatcheryDeliverySaleRepository.find(hatcheryDeliverySale.getId());

        if (hatcheryDeliverySale1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
        }

        hatcheryDeliverySale1.setReasonOfCancel(hatcheryDeliverySale1.getReasonOfCancel());
        hatcheryDeliverySale1.setStatus(HatcherEntryRepository.STATUS_CANCELED);
        hatcheryDeliverySale1.getCustomerSaleOrder().setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);

        StockMove stockMove = hatcheryDeliverySale1.getDispatchStock();
        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }
        return hatcheryDeliverySaleRepository.save(hatcheryDeliverySale1);
    }
    @Transactional
    public HatcheryDeliverySale Revalidate(HatcheryDeliverySale hatcheryDeliverySale) throws AxelorException {
        HatcheryDeliverySale hatcheryDeliverySale1 = hatcheryDeliverySaleRepository.find(hatcheryDeliverySale.getId());

        if (hatcheryDeliverySale1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
        }
        hatcheryDeliverySale1.setStatus(HatcheryEggExitRepository.STATUS_NOT_EXECUTED);
        StockMove stockMove = hatcheryDeliverySale1.getDispatchStock();

        if (stockMove != null) {
            stockMoveService.goBackToDraft(stockMove);
        }
        return hatcheryDeliverySaleRepository.save(hatcheryDeliverySale1);
    }
}




