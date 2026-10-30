package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.LivestockFeedProduction;
import com.gl.apps.pf.db.LivestockFeedProductionLine;
import com.gl.apps.pf.db.repo.LivestockFeedProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class LivestockFeedProductionServiceImpl implements LivestockFeedProductionService {
    @Inject private LivestockFeedProductionRepository livestockFeedProductionRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockLocationRepository stockLocationRepository;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryFarmingRepository;

    @Transactional
    public LivestockFeedProduction Validate(LivestockFeedProduction livestockFeedProduction) throws AxelorException {
        LivestockFeedProduction production = livestockFeedProductionRepository.find(livestockFeedProduction.getId());

        if (production == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_PRODUCTION_NOT_FOUND));
        }

        if (production.getLiveStockFeedFactory() == null) {
            throw new AxelorException(
                    production,
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Livestock feed factory is required."));
        }

        StockLocation factoryLocation = production.getLiveStockFeedFactory().getPhysicalLocation();
        if (factoryLocation == null) {
            throw new AxelorException(
                    production,
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No physical stock location specified for factory: " + production.getLiveStockFeedFactory().getName()));
        }

        if (!production.getLivestockFeedProductionLineList().isEmpty()) {
            for (LivestockFeedProductionLine line : production.getLivestockFeedProductionLineList()) {
                if (line.getCompositionProduct() != null && production.getQuantityProduced() != null) {
                    line.setQuantityProduct(line.getCompositionProduct().divide(new BigDecimal(100)).multiply(production.getQuantityProduced()));
                }
                if (line.getQuantityProduct() != null && line.getUnitPriceProduct() != null) {
                    line.setTotalAmount(line.getQuantityProduct().multiply(line.getUnitPriceProduct()));
                }
            }
        }

        AppPoultryFarming app = appPoultryFarmingRepository.all().fetchOne();
        StockLocation virtualLocation = app != null ? app.getVirtualProductionLocation() : null;
        if (virtualLocation == null && app != null) {
            virtualLocation = app.getVirtualConsumptionLocation();
        }
        if (virtualLocation == null) {
            virtualLocation = stockLocationRepository.all().filter("self.typeSelect = ?1", StockLocationRepository.TYPE_VIRTUAL).fetchOne();
        }

        Company company = factoryLocation.getCompany();
        if (company == null && production.getLiveStockFeedFactory().getUnit() != null) {
            company = production.getLiveStockFeedFactory().getUnit().getCompany();
        }
        if (company == null && AuthUtils.getUser() != null) {
            company = AuthUtils.getUser().getActiveCompany();
        }

        StockMove dispatchStock = production.getDispatchStock();
        if (dispatchStock == null) {
            Map<String, Object> values = new HashMap<>();
            values.put("company", company);
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            values.put("fromStockLocation", factoryLocation);
            values.put("toStockLocation", factoryLocation);
            dispatchStock = stockMoveRepo.create(values);
        } else {
            dispatchStock.clearStockMoveLineList();
        }

        dispatchStock.setLivestockFeedProduction(production);
        dispatchStock.setSubType(StockMoveRepository.SUB_TYPE_LIVESTOCK_FEED_PRODUCTION);

        // 1. Raw material lines consumed from the factory warehouse (from factoryLocation to virtualLocation)
        if (!production.getLivestockFeedProductionLineList().isEmpty()) {
            for (LivestockFeedProductionLine line : production.getLivestockFeedProductionLineList()) {
                if (line != null && line.getProduct() != null && line.getQuantityProduct() != null && line.getQuantityProduct().compareTo(BigDecimal.ZERO) > 0) {
                    StockMoveLine sml = new StockMoveLine();
                    sml.setProduct(line.getProduct());
                    sml.setProductName(line.getProduct().getName());
                    sml.setUnit(line.getProduct().getUnit());
                    sml.setQty(line.getQuantityProduct());
                    sml.setRealQty(line.getQuantityProduct());
                    sml.setUnitPriceUntaxed(line.getUnitPriceProduct());
                    sml.setFromStockLocation(factoryLocation);
                    sml.setToStockLocation(virtualLocation != null ? virtualLocation : factoryLocation);
                    dispatchStock.addStockMoveLineListItem(sml);
                }
            }
        }

        // 2. Produced feed added into liveStockFeedFactory.getPhysicalLocation() (from virtualLocation to factoryLocation)
        if (production.getProduct() != null && production.getQuantityProduced() != null && production.getQuantityProduced().compareTo(BigDecimal.ZERO) > 0) {
            StockMoveLine producedLine = new StockMoveLine();
            producedLine.setProduct(production.getProduct());
            producedLine.setProductName(production.getProductName() != null ? production.getProductName() : production.getProduct().getName());
            producedLine.setUnit(production.getUnit() != null ? production.getUnit() : production.getProduct().getUnit());
            producedLine.setQty(production.getQuantityProduced());
            producedLine.setRealQty(production.getQuantityProduced());
            producedLine.setUnitPriceUntaxed(production.getSalePrice());
            producedLine.setFromStockLocation(virtualLocation != null ? virtualLocation : factoryLocation);
            producedLine.setToStockLocation(factoryLocation);
            dispatchStock.addStockMoveLineListItem(producedLine);
        }

        dispatchStock = stockMoveRepo.save(dispatchStock);
        stockMoveService.plan(dispatchStock);
        stockMoveService.realize(dispatchStock, true);
        dispatchStock.setRealDate(LocalDate.now());
        dispatchStock = stockMoveRepo.save(dispatchStock);

        production.setDispatchStock(dispatchStock);
        production.setStatus(LivestockFeedProductionRepository.STATUS_EXECUTED);
        return livestockFeedProductionRepository.save(production);
    }

    @Transactional
    public LivestockFeedProduction Cancel(LivestockFeedProduction livestockFeedProduction) throws AxelorException {
        LivestockFeedProduction production = livestockFeedProductionRepository.find(livestockFeedProduction.getId());

        if (production == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_PRODUCTION_NOT_FOUND));
        }

        if (production.getDispatchStock() != null) {
            stockMoveService.cancel(production.getDispatchStock());
            stockMoveService.goBackToDraft(production.getDispatchStock());
        }

        production.setStatus(LivestockFeedProductionRepository.STATUS_CANCELED);
        return livestockFeedProductionRepository.save(production);
    }
}
