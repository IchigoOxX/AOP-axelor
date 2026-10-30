package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.PurchaseRequestLine;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.PurchaseRequestService;
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
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.ProductOrder;
import com.gl.apps.pf.db.ProductOrderLine;
import com.gl.apps.pf.db.StartElevMovement;
import com.gl.apps.pf.db.repo.CenterRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.db.repo.ProductOrderLineRepository;
import com.gl.apps.pf.db.repo.ProductOrderRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ProductOrderServiceImpl implements ProductOrderService{
    @Inject
    private ProductOrderRepository productOrderRepository;
    @Inject private ProductOrderLineRepository productOrderLineRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private PurchaseRequestRepository purchaseRequestRepository;
    @Inject private StockMoveService stockMoveService;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public ProductOrder Send(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(order);
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
        order.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_PRODUCTS);
        stockMoveService.plan(dispatcherMove);
        order.setStatus(ProductOrderRepository.STATUS_EXECUTING);
        return productOrderRepository.save(order);

    }

    @Transactional
    public ProductOrder CancelSend(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = order.getDispatchStock();
        if(dispatcherMove != null) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        order.setStatus(ProductOrderRepository.STATUS_CANCELED);
        return productOrderRepository.save(order);

    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(ProductOrder productOrder)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (productOrder == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        ProductOrder order = productOrderRepository.find(productOrder.getId());

        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ORDER_PRODUCT_NOT_SPECIFIED));
        }

        StockLocation virtualStartupLocation = app.getVirtualStartupLocation();
        StockLocation virtualReproLocation = app.getVirtualProductionLocation();
        StockLocation virtualHatcheryLocation = app.getVirtualHatcheryLocation();
        StockLocation chairLocation = app.getChairDefaultLocation();

        StockMove stockMove;

        if (order.getDispatchStock() == null) {

            Map<String, Object> values = new HashMap<>();
            if( order.getCenter() != null){
                values.put("company", order.getCenter().getUnit().getCompany());
            }
            else if (order.getHatchery() != null) {
                values.put("company", order.getHatchery().getUnit().getCompany());
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            // FOR CENTER TYPE STARTUP
            if (order.getCenter()!= null && order.getCenter().getType() == CenterRepository.TYPE_STARTUP && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_STARTUP ){
                if(virtualStartupLocation == null ){
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }
                else {
                    values.put("fromStockLocation", virtualStartupLocation);
                    values.put("toStockLocation", virtualStartupLocation);
                }

            }
            if (order.getCenter()!= null &&  order.getCenter().getType() == CenterRepository.TYPE_STARTUP && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_REPRODUCTION ) {
                if(virtualStartupLocation != null ){
                    values.put("fromStockLocation", virtualStartupLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }
                if(virtualReproLocation != null){
                    values.put("toStockLocation", virtualReproLocation);
                }
               else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }
            }
            if (order.getCenter()!= null &&  order.getCenter().getType() == CenterRepository.TYPE_STARTUP && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_BROILER ) {
                if(virtualStartupLocation != null ){
                    values.put("fromStockLocation", virtualStartupLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }
                if(chairLocation != null){
                    values.put("toStockLocation", chairLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
                }

            }
            // FOR CENTER TYPE REPRO
            if ( order.getCenter()!= null && order.getCenter().getType() == CenterRepository.TYPE_REPRODUCTION  && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_REPRODUCTION) {
                if(virtualReproLocation == null){
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }
                else {
                    values.put("fromStockLocation", virtualReproLocation);
                    values.put("toStockLocation", virtualReproLocation);
                }
            }

            if ( order.getCenter()!= null && order.getCenter().getType() == CenterRepository.TYPE_REPRODUCTION && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_STARTUP){
                if(virtualReproLocation != null){
                    values.put("fromStockLocation", virtualReproLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }
                if(virtualStartupLocation != null){
                    values.put("toStockLocation", virtualStartupLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }
            }
            if ( order.getCenter()!= null && order.getCenter().getType() == CenterRepository.TYPE_REPRODUCTION && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_BROILER){
                if(virtualReproLocation != null ){
                    values.put("fromStockLocation", virtualReproLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }
                if(chairLocation != null ){
                    values.put("toStockLocation", chairLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
                }

            }

            // LIVESTOCK FEED FACTORY
//            if (order.getCenter()!= null && order.getFromLiveStockFeedFactory()!= null && order.getCenter().getType() == CenterRepository.TYPE_STARTUP) {
//                if(virtualStartupLocation != null){
//                    values.put("fromStockLocation", virtualStartupLocation);
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
//                }
//                if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                    values.put("toStockLocation", order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get("No stock location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                }
//
//            }

//            if (order.getCenter()!= null && order.getFromLiveStockFeedFactory()!= null && order.getCenter().getType() == CenterRepository.TYPE_REPRODUCTION) {
//                if(virtualReproLocation != null ){
//                    values.put("fromStockLocation", virtualReproLocation);
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
//                }
//                if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                    values.put("toStockLocation", order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get("No stock location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                }
//            }
//            if (order.getCenter()!= null && order.getFromLiveStockFeedFactory()!= null && order.getCenter().getType() == CenterRepository.TYPE_BROILER) {
//                if(chairLocation != null ){
//                    values.put("fromStockLocation", chairLocation);
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
//                }
//                if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                    values.put("toStockLocation", order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get("No stock location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                }
//            }

            // FOR HATCHERY
            if (order.getHatchery()!= null && order.getFromCenter()!= null && order.getFromCenter().getType() == CenterRepository.TYPE_STARTUP ){
                if(virtualHatcheryLocation != null ){
                    values.put("fromStockLocation", virtualHatcheryLocation);
                }
                else{
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                }
                if(virtualStartupLocation != null) {
                    values.put("toStockLocation", virtualStartupLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_STARTUP_LOCATION_NOT_SPECIFIED));
                }

            }
            if (order.getHatchery()!= null && order.getFromCenter() != null && order.getFromCenter().getType() == CenterRepository.TYPE_REPRODUCTION ){
                if(virtualHatcheryLocation != null ){
                    values.put("fromStockLocation", virtualHatcheryLocation);
                }
                else{
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                }
                if(virtualReproLocation != null ){
                    values.put("toStockLocation", virtualReproLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
                }
            }
            if (order.getHatchery()!= null && order.getFromCenter() != null && order.getFromCenter().getType() == CenterRepository.TYPE_BROILER ){
                if(virtualHatcheryLocation != null ){
                    values.put("fromStockLocation", virtualHatcheryLocation);
                }
                else{
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                }
                if(chairLocation != null ){
                    values.put("toStockLocation", chairLocation);
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
                }
            }

//            if (order.getHatchery()!= null && order.getFromLiveStockFeedFactory()!= null){
//                if(virtualHatcheryLocation != null ){
//                    values.put("fromStockLocation", virtualHatcheryLocation);
//                }
//                else{
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
//                }
//                if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                    values.put("toStockLocation", order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                }
//                else {
//                    throw new AxelorException(
//                            TraceBackRepository.CATEGORY_MISSING_FIELD,
//                            I18n.get("No stock location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                }
//            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(order.getCenter());
            stockMove.setProductOrder(order);
        }


        else {
            stockMove = order.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!order.getProductOrderLineList().isEmpty() ) {
            for (ProductOrderLine line : order.getProductOrderLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    //center => center
                    if (order.getCenter() != null && order.getBreedingShed() == null && order.getFromCenter() != null) {
                        sm.setProduct(line.getProduct());
                        sm.setQty(line.getQuantityProduct());
                        if (order.getCenter().getPhysicalLocation() != null) {
                            sm.setFromStockLocation(order.getCenter().getPhysicalLocation());
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No physical location was specified for center : " + order.getCenter().getName() + " !"));
                        }
                        if(order.getFromCenter().getPhysicalLocation() != null){
                            sm.setToStockLocation(order.getFromCenter().getPhysicalLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No physical location was specified for center : " + order.getFromCenter().getName() + " !"));
                        }
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }
                    // center => usine d'aliment
//                    if (order.getCenter() != null && order.getBreedingShed() == null && order.getFromLiveStockFeedFactory() != null) {
//                        sm.setProduct(line.getProduct());
//                        sm.setQty(line.getQuantityProduct());
//                        if(order.getCenter().getPhysicalLocation() != null) {
//                            sm.setFromStockLocation(order.getCenter().getPhysicalLocation());
//                        }
//                        else {
//                            throw new AxelorException(
//                                TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                I18n.get("No physical location was specified for center : " + order.getCenter().getName() + " !"));
//                        }
//                        if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                            sm.setToStockLocation(order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                        }
//                        else {
//                            throw new AxelorException(
//                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                    I18n.get("No physical location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                        }
//                        sm.setProductName(sm.getProduct().getName());
//                        sm.setUnit(sm.getProduct().getUnit());
//                    }
                    // batiment => center
                    if (order.getBreedingShed() != null && order.getFromCenter() != null) {
                        sm.setProduct(line.getProduct());
                        sm.setQty(line.getQuantityProduct());
                        if(order.getBreedingShed().getStockLocation() != null ){
                            sm.setFromStockLocation(order.getBreedingShed().getStockLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No physical location was specified for breeding shed : " + order.getBreedingShed().getName() + " !"));
                        }
                        if(order.getFromCenter().getPhysicalLocation() != null){
                            sm.setToStockLocation(order.getFromCenter().getPhysicalLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No physical location was specified for center : " + order.getFromCenter().getName() + " !"));
                        }
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }
                    // batiment => usine d'aliment
//                    if (order.getBreedingShed() != null && order.getFromLiveStockFeedFactory() != null) {
//                        sm.setProduct(line.getProduct());
//                        sm.setQty(line.getQuantityProduct());
//                        if(order.getBreedingShed().getStockLocation() != null){
//                            sm.setFromStockLocation(order.getBreedingShed().getStockLocation());
//                        }
//                        else {
//                            throw new AxelorException(
//                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                    I18n.get("No physical location was specified for breeding shed : " + order.getBreedingShed().getName() + " !"));
//                        }
//                        if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null){
//                            sm.setToStockLocation(order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                        }
//                        else {
//                            throw new AxelorException(
//                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                    I18n.get("No physical location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                        }
//                        sm.setProductName(sm.getProduct().getName());
//                        sm.setUnit(sm.getProduct().getUnit());
//                    }
                    // hatchery => center
                    if (order.getHatchery() != null && order.getFromCenter() != null) {
                        sm.setProduct(line.getProduct());
                        sm.setQty(line.getQuantityProduct());
                        if(virtualHatcheryLocation != null){
                            sm.setFromStockLocation(virtualHatcheryLocation);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
                        }
                        if(order.getFromCenter().getPhysicalLocation() != null){
                            sm.setToStockLocation(order.getFromCenter().getPhysicalLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No physical location was specified for center : " + order.getFromCenter().getName() + " !"));
                        }

                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                    }
                    // hatchery => livestock factory
//                    if (order.getHatchery() != null && order.getFromLiveStockFeedFactory() != null) {
//                        sm.setProduct(line.getProduct());
//                        sm.setQty(line.getQuantityProduct());
//                        if(virtualHatcheryLocation != null){
//                            sm.setFromStockLocation(virtualHatcheryLocation);
//                        }
//                        else {
//                            throw new AxelorException(
//                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                    I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_HATCHERY_LOCATION_NOT_SPECIFIED));
//                        }
//                        if(order.getFromLiveStockFeedFactory().getPhysicalLocation() != null ){
//                            sm.setToStockLocation(order.getFromLiveStockFeedFactory().getPhysicalLocation());
//                        }
//                        else {
//                            throw new AxelorException(
//                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                                    I18n.get("No physical location was specified for livestock feed factory : " + order.getFromLiveStockFeedFactory().getName() + " !"));
//                        }
//
//                        sm.setProductName(sm.getProduct().getName());
//                        sm.setUnit(sm.getProduct().getUnit());
//                    }
                    stockMove.addStockMoveLineListItem(sm);
                }
            }
        }
        return stockMoveRepo.save(stockMove);

    }

    @Transactional
    public ProductOrder Validate(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());

        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = order.getDispatchStock();
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

        if (!dispatcherMove.getStockMoveLineList().isEmpty()) {
            dispatcherMove.getStockMoveLineList().forEach(
                    lineMove -> {
                        if (lineMove != null) {
                            if (!order.getProductOrderLineList().isEmpty()) {
                                order.getProductOrderLineList().forEach(
                                    line -> {
                                        if (line != null && line.getProduct() == lineMove.getProduct()) {
                                        lineMove.setUnitPriceUntaxed(line.getUnitPriceProduct());
                                        lineMove.setUnitPriceTaxed(line.getUnitPriceProduct());
                                        }
                                    });
                            }
                        }
                    });
        }
        dispatcherMove.setProductRelease(order);
        stockMoveService.realize(dispatcherMove);
        order.setStatus(ProductOrderRepository.STATUS_VALIDATION);
        return productOrderRepository.save(order);

        }

    @Transactional
    public ProductOrder Receive(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = order.getDispatchStock();
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
        if (!dispatcherMove.getStockMoveLineList().isEmpty()) {
            dispatcherMove.getStockMoveLineList().forEach(
                lineMove -> {
                    if (lineMove != null) {
                        if (!order.getProductOrderLineList().isEmpty()) {
                            order.getProductOrderLineList().forEach(
                                    line -> {
                                        if (line != null && line.getProduct() == lineMove.getProduct()) {
                                            lineMove.setRealQty(line.getReelQuantityProduct());
                                            lineMove.setQtyInvoiced(line.getReelQuantityProduct());
                                        }
                                    });
                        }
                    }
                });
        }
        dispatcherMove.setReceiptOrder(order);
        stockMoveService.realize(dispatcherMove);
        dispatcherMove.setRealDate(LocalDate.now());
        order.setStatus(ProductOrderRepository.STATUS_RECEIVED);

        return productOrderRepository.save(order);
    }


    @Transactional
    public ProductOrder CancelReceive(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = order.getDispatchStock();

        if (!dispatcherMove.getStockMoveLineList().isEmpty()) {
            dispatcherMove.getStockMoveLineList().forEach(
                lineMove -> {
                    if (lineMove != null) {
                        if (!order.getProductOrderLineList().isEmpty()) {
                            order.getProductOrderLineList().forEach(
                                line -> {
                                    if (line != null && line.getProduct() == lineMove.getProduct()) {
                                        lineMove.setRealQty(BigDecimal.ZERO);
                                        lineMove.setQtyInvoiced(BigDecimal.ZERO);
                                    }
                                });
                        }
                    }
                });
        }
        if(dispatcherMove != null ){
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
            stockMoveService.plan(dispatcherMove);
        }

        order.setStatus(ProductOrderRepository.STATUS_RECEPTION_CANCELED);

        return productOrderRepository.save(order);
    }

    @Transactional
    public ProductOrder SendLiveStockFeedFactory(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        PurchaseRequest purchaseRequest = CreateOrUpdatePurchaseRequest(order);
        if(purchaseRequest != null ){
            order.setPurchaseRequest(purchaseRequest);
            purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_REQUESTED);
        }
        order.setStatus(ProductOrderRepository.STATUS_EXECUTING);

        return productOrderRepository.save(order);
    }

    @Transactional
    public ProductOrder CancelSendLiveStockFeedFactory(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());
        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        PurchaseRequest purchaseRequest = order.getPurchaseRequest();
        if(purchaseRequest != null ) {
            purchaseRequest.setStatusSelect(PurchaseRequestRepository.STATUS_DRAFT);
        }
        order.setStatus(ProductOrderRepository.STATUS_CANCELED);

        return productOrderRepository.save(order);
    }

    @Transactional
    public PurchaseRequest CreateOrUpdatePurchaseRequest(ProductOrder productOrder)
            throws AxelorException {

        if (productOrder == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        ProductOrder order = productOrderRepository.find(productOrder.getId());

        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ORDER_PRODUCT_NOT_SPECIFIED));
        }

        PurchaseRequest purchaseRequest;

        if (order.getPurchaseRequest() == null ) {
            if(order.getUnit().getCompany().getStockConfig().getReceiptDefaultStockLocation() != null) {
                Map<String, Object> values = new HashMap<>();
                values.put("company", order.getUnit().getCompany());
                if(order.getUnit().getCompany().getStockConfig().getReceiptDefaultStockLocation() != null){
                    values.put("stockLocation", order.getUnit().getCompany().getStockConfig().getReceiptDefaultStockLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.RECEIPT_DEFAULT_STOCK_LOCATION_NOT_SPECIFIED));
                }
                purchaseRequest = purchaseRequestRepository.create(values);

            }
            else  throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.RECEIPT_DEFAULT_LOCATION_NOT_SPECIFIED));
        }

        else {
            purchaseRequest = order.getPurchaseRequest();
            purchaseRequest.clearPurchaseRequestLineList();
        }
        if (!order.getProductOrderLineList().isEmpty()) {
            order.getProductOrderLineList().forEach(
                line -> {
                    if (line != null) {
                        PurchaseRequestLine sm = new PurchaseRequestLine();
                            sm.setProduct(line.getProduct());
                            sm.setQuantity(line.getQuantityProduct());
                            sm.setUnit(sm.getProduct().getUnit());
                            purchaseRequest.addPurchaseRequestLineListItem(sm);
                    }
                });
        }
        return purchaseRequestRepository.save(purchaseRequest);

    }


    @Transactional
    public ProductOrder CancelReleaseOrder(ProductOrder productOrder) throws AxelorException {
        ProductOrder order = productOrderRepository.find(productOrder.getId());

        if (order == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_ORDER_NOT_FOUND));
        }

        StockMove dispatcherMove = order.getDispatchStock();

        if (!dispatcherMove.getStockMoveLineList().isEmpty()) {
            dispatcherMove.getStockMoveLineList().forEach(
                lineMove -> {
                    if (lineMove != null) {
                        if (!order.getProductOrderLineList().isEmpty()) {
                            order.getProductOrderLineList().forEach(
                                line -> {
                                    if (line != null && line.getProduct() == lineMove.getProduct()) {
                                        lineMove.setUnitPriceUntaxed(BigDecimal.ZERO);
                                        lineMove.setUnitPriceTaxed(BigDecimal.ZERO);
                                    }
                                });
                        }
                    }
                });
        }
        //stockMoveService.realize(dispatcherMove);
        order.setStatus(ProductOrderRepository.STATUS_RELEASE_CANCELED);
        return productOrderRepository.save(order);

    }


}




