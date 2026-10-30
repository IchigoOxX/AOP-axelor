package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.ElevReform;
import com.gl.apps.pf.db.MaintenanceEnergyConsumption;
import com.gl.apps.pf.db.repo.ElevReformRepository;
import com.gl.apps.pf.db.repo.MaintenanceEnergyConsumptionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MaintenanceEnergyConsumptionServiceImpl implements MaintenanceEnergyConsumptionService {
    @Inject
    private MaintenanceEnergyConsumptionRepository maintenanceEnergyConsumptionRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;

    @Transactional
    public MaintenanceEnergyConsumption Validate(MaintenanceEnergyConsumption maintenanceEnergyConsumption) throws AxelorException {

        MaintenanceEnergyConsumption maintenanceEnergyConsumption1 = maintenanceEnergyConsumptionRepository.find(maintenanceEnergyConsumption.getId());

        if (maintenanceEnergyConsumption1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_ENERGY_CONSUMPTION_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(maintenanceEnergyConsumption1);
        maintenanceEnergyConsumption1.setDispatchStock(dispatcherMove);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);
        maintenanceEnergyConsumption1.setStatus(MaintenanceEnergyConsumptionRepository.STATUS_EXECUTED);

        return maintenanceEnergyConsumptionRepository.save(maintenanceEnergyConsumption1);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(MaintenanceEnergyConsumption maintenanceEnergyConsumption) throws AxelorException {
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        if (maintenanceEnergyConsumption == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_ENERGY_CONSUMPTION_NOT_FOUND));
        }

        MaintenanceEnergyConsumption consumption = maintenanceEnergyConsumptionRepository.find(maintenanceEnergyConsumption.getId());

        if (consumption == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_ENERGY_CONSUMPTION_NOT_SPECIFIED));
        }

        StockLocation maintenanceLocation = app.getMaintenanceLocation();

        StockMove stockMove;

        if (consumption.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            values.put("company", AuthUtils.getUser().getActiveCompany());
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if (maintenanceLocation == null) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_LOCATION_NOT_SPECIFIED));
            } else {
                values.put("fromStockLocation", maintenanceLocation);
                values.put("toStockLocation", maintenanceLocation);
            }

            stockMove = stockMoveRepo.create(values);

        } else {
            stockMove = consumption.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if(consumption.getQty().compareTo(BigDecimal.ZERO) != 0)  {
            StockMoveLine sm = new StockMoveLine();
                sm.setProduct(consumption.getProduct());
                sm.setQty(consumption.getQty());
                sm.setRealQty(consumption.getQty());
                sm.setFromStockLocation(maintenanceLocation);
                sm.setToStockLocation(maintenanceLocation);
                sm.setProductName(sm.getProduct().getName());
                sm.setUnit(sm.getProduct().getUnit());
                stockMove.addStockMoveLineListItem(sm);
        }
        return stockMoveRepo.save(stockMove);
    }
}
