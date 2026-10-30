package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.maintenance.db.MachineProductLine;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.repo.MachineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.IncubatorEntryLine;
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.repo.PharmacyTreatmentRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MaintenanceMachineServiceImpl implements MaintenanceMachineService {
    @Inject
    private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private MachineRepository machineRepository;
    @Transactional
    public Machine Validate(Machine machine) throws AxelorException {
        Machine machine1 = machineRepository.find(machine.getId());
        if (machine1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_MACHINE_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(machine1);
        machine1.setDispatchStock(dispatcherMove);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove, true);

        return machineRepository.save(machine1);

    }
    @Transactional
    public StockMove CreateOrUpdateStockMove(Machine machine) throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (machine == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_MACHINE_NOT_FOUND));
        }

        Machine machine1 = machineRepository.find(machine.getId());

        if (machine1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_MACHINE_ENTRY_NOT_SPECIFIED));
        }

        StockLocation maintenanceLocation = app.getMaintenanceLocation();
        StockMove stockMove;

        if (machine1.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            values.put("company", AuthUtils.getUser().getActiveCompany());
            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if(maintenanceLocation != null ){
                values.put("fromStockLocation", maintenanceLocation);
                values.put("toStockLocation", maintenanceLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.MAINTENANCE_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
        }
        else {
            stockMove = machine1.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!machine1.getSparePartProductsList().isEmpty()) {
            for (MachineProductLine line : machine1.getSparePartProductsList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getQty() != 0) {
                        sm.setProduct(line.getProduct());
                        sm.setQty(BigDecimal.valueOf(line.getQty()));
                        sm.setRealQty(BigDecimal.valueOf(line.getQty()));
                        sm.setFromStockLocation(maintenanceLocation);
                        sm.setTrackingNumber(line.getTrackingNumber());
                        sm.setToStockLocation(maintenanceLocation);
                        sm.setProductName(sm.getProduct().getName());
                        sm.setUnit(sm.getProduct().getUnit());
                        stockMove.addStockMoveLineListItem(sm);
                    }
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

}
