package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
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
import com.gl.apps.pf.db.ProductConsumption;
import com.gl.apps.pf.db.ProductConsumptionLine;
import com.gl.apps.pf.db.ProductOrder;
import com.gl.apps.pf.db.repo.CenterRepository;
import com.gl.apps.pf.db.repo.ProductConsumptionRepository;
import com.gl.apps.pf.db.repo.ProductOrderRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ProductConsumptionServiceImpl implements ProductConsumptionService{
    @Inject
    private ProductConsumptionRepository productConsumptionRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public ProductConsumption Validate(ProductConsumption productConsumption) throws AxelorException {
        ProductConsumption productConsumpt = productConsumptionRepository.find(productConsumption.getId());
        if (productConsumpt == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_FOUND));
        }
        StockMove dispatcherMove = CreateOrUpdateStockMove(productConsumpt);
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
        productConsumpt.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_PRODUCTS);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);
        dispatcherMove.setRealDate(LocalDate.now());

        productConsumpt.setStatus(ProductConsumptionRepository.STATUS_EXECUTING);
        return productConsumptionRepository.save(productConsumpt);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(ProductConsumption productConsumption)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (productConsumption == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_FOUND));
        }

        ProductConsumption productConsumpt = productConsumptionRepository.find(productConsumption.getId());

        if (productConsumpt == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_SPECIFIED));
        }

        StockLocation virtualStartupLocation = app.getVirtualStartupLocation();
        StockLocation virtualReproLocation = app.getVirtualProductionLocation();
        StockLocation virtualConsumptionLocation = app.getVirtualConsumptionLocation();
        StockLocation virtualHatcheryLocation = app.getVirtualHatcheryLocation();
        StockLocation chairLocation = app.getChairDefaultLocation();
        StockMove stockMove;

        if (productConsumpt.getDispatchStock() == null) {

            Map<String, Object> values = new HashMap<>();
            if( productConsumpt.getCenter() != null){
                values.put("company", productConsumpt.getCenter().getUnit().getCompany());
            }
            else if (productConsumpt.getHatchery() != null) {
                values.put("company", productConsumpt.getHatchery().getUnit().getCompany());
            }
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(virtualConsumptionLocation != null) {
                values.put("toStockLocation", virtualConsumptionLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_CONSUMPTION_LOCATION_NOT_SPECIFIED));
            }

            if (productConsumpt.getCenter() != null && productConsumpt.getCenter().getType() == CenterRepository.TYPE_STARTUP ){
                if(virtualStartupLocation != null ){
                    values.put("fromStockLocation", virtualStartupLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }
            }

            if (productConsumpt.getCenter() != null && productConsumpt.getCenter().getType() == CenterRepository.TYPE_REPRODUCTION ){
                if(virtualReproLocation != null ){
                    values.put("fromStockLocation", virtualReproLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }

            }
            if (productConsumpt.getCenter() != null && productConsumpt.getCenter().getType() == CenterRepository.TYPE_BROILER ){
                if(chairLocation != null ){
                    values.put("fromStockLocation", chairLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
                }
            }
            if (productConsumpt.getCenter() == null && productConsumpt.getHatchery() != null ){
                if(virtualHatcheryLocation != null){
                    values.put("fromStockLocation", virtualHatcheryLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                }
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(productConsumpt.getCenter());
            stockMove.setProductConsumption(productConsumpt);
        }

        else {
            stockMove = productConsumpt.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!productConsumpt.getProductConsumptionLineList().isEmpty()) {
            for (ProductConsumptionLine line : productConsumpt.getProductConsumptionLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    //center => consom location
                    if (productConsumpt.getCenter() != null && productConsumpt.getBreedingShed() == null) {
                        sm.setProduct(line.getProduct());
                        sm.setQty(line.getQuantityProduct());
                        sm.setRealQty(line.getQuantityProduct());
                        sm.setUnitPriceUntaxed(line.getUnitPriceProduct());
                        if (productConsumpt.getCenter().getPhysicalLocation() != null) {
                            sm.setFromStockLocation(productConsumpt.getCenter().getPhysicalLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for center : " + productConsumpt.getCenter().getName() + " !"));
                        }
                        sm.setToStockLocation(virtualConsumptionLocation);
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }
                    // batiment => consom location
                    if (productConsumpt.getBreedingShed() != null) {
                        sm.setProduct(line.getProduct());
                        sm.setRealQty(line.getQuantityProduct());
                        sm.setQty(line.getQuantityProduct());
                        sm.setUnitPriceUntaxed(line.getUnitPriceProduct());
                        if(productConsumpt.getBreedingShed().getStockLocation() != null ){
                            sm.setFromStockLocation(productConsumpt.getBreedingShed().getStockLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed : " + productConsumpt.getBreedingShed().getName() + " !"));
                        }

                        sm.setToStockLocation(virtualConsumptionLocation);
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }
                    // hatchery => consom location
                    if (productConsumpt.getHatchery() != null && productConsumpt.getCenter() == null && productConsumpt.getBreedingShed() == null) {
                        sm.setProduct(line.getProduct());
                        sm.setRealQty(line.getQuantityProduct());
                        sm.setQty(line.getQuantityProduct());
                        sm.setUnitPriceUntaxed(line.getUnitPriceProduct());
                        sm.setFromStockLocation(virtualHatcheryLocation);
                        sm.setToStockLocation(virtualConsumptionLocation);
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }

                    stockMove.addStockMoveLineListItem(sm);
                }
            }
        }
        return stockMoveRepo.save(stockMove);

    }
    @Transactional
    public ProductConsumption Cancel(ProductConsumption productConsumption) throws AxelorException {
        ProductConsumption productConsumpt = productConsumptionRepository.find(productConsumption.getId());
        if (productConsumpt == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONSUMPTION_NOT_FOUND));
        }
        StockMove dispatcherMove = productConsumpt.getDispatchStock();
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_PRODUCTS);

        if( dispatcherMove != null ) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        productConsumpt.setStatus(ProductConsumptionRepository.STATUS_CANCELED);

        return productConsumptionRepository.save(productConsumpt);
    }
}




