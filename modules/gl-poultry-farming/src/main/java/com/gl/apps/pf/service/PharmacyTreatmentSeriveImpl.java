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
import com.gl.apps.pf.db.PharmacyTreatment;
import com.gl.apps.pf.db.PharmacyTreatmentLine;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.repo.PharmacyTreatmentRepository;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PharmacyTreatmentSeriveImpl implements PharmacyTreatmentService{
    @Inject
    private PharmacyTreatmentRepository pharmacyTreatmentRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private StockLocationUtilsService stockLocationService;

    @Transactional
    public PharmacyTreatment Validate(PharmacyTreatment pharmacyTreatment) throws AxelorException {
        PharmacyTreatment treatment = pharmacyTreatmentRepository.find(pharmacyTreatment.getId());
        if (treatment == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PHARMACY_TREATMENT_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(treatment);
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
        treatment.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_TREATMENTS_VACCINATIONS);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove, true);
        dispatcherMove.setRealDate(LocalDate.now());

        treatment.setStatus(PharmacyTreatmentRepository.STATUS_EXECUTING);
        return pharmacyTreatmentRepository.save(treatment);

    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(PharmacyTreatment pharmacyTreatment)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (pharmacyTreatment == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PHARMACY_TREATMENT_NOT_FOUND));
        }

        PharmacyTreatment treatment = pharmacyTreatmentRepository.find(pharmacyTreatment.getId());

        if (treatment == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PHARMACY_TREATMENT_NOT_SPECIFIED));
        }

        StockLocation pharmacyLocation = app.getVirtualPharmacyLocation();
        StockLocation virtualConsumptionLocation = app.getVirtualConsumptionLocation();
        StockMove stockMove;

        if (treatment.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(treatment.getCenter().getUnit().getCompany() != null) {
                values.put("company", treatment.getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(pharmacyTreatment.getCenter() != null && pharmacyTreatment.getBreedingShed() == null) {
                if(pharmacyTreatment.getCenter().getPhysicalLocation()  != null ) {
                    values.put("fromStockLocation", pharmacyTreatment.getCenter().getPhysicalLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("No stock location was specified for center : " + pharmacyTreatment.getCenter().getName() + " !"));
                }
            }
            else if(pharmacyTreatment.getBreedingShed() != null ){
                if(pharmacyTreatment.getBreedingShed().getStockLocation() != null ) {
                    values.put("fromStockLocation", pharmacyTreatment.getBreedingShed().getStockLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("No stock location was specified for breeding shed : " + pharmacyTreatment.getBreedingShed().getName() + " !"));
                }
            }

            if(virtualConsumptionLocation != null ) {
                values.put("toStockLocation", virtualConsumptionLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_CONSUMPTION_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(treatment.getCenter());
            stockMove.setPharmacyTreatment(treatment);
        }
        else {
            stockMove = treatment.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!treatment.getPharmacyTreatmentLineList().isEmpty()) {
            for (PharmacyTreatmentLine line : treatment.getPharmacyTreatmentLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if(pharmacyTreatment.getCenter() != null && pharmacyTreatment.getBreedingShed() == null )  {
                        sm.setFromStockLocation(pharmacyTreatment.getCenter().getPhysicalLocation());
                    }
                    else if (pharmacyTreatment.getBreedingShed() != null) {
                        sm.setFromStockLocation(pharmacyTreatment.getBreedingShed().getStockLocation());
                    }
                    sm.setProduct(line.getMedicalTreatment());
                    if (line.getPharmacyTreatment().getTotalNumbers().compareTo(0) > 0) {
                        sm.setQty(line.getDosePerSubject().multiply(BigDecimal.valueOf(line.getPharmacyTreatment().getTotalNumbers())));
                    }
                    else {
                        sm.setQty(line.getDosePerSubject().multiply((BigDecimal.valueOf(line.getPharmacyTreatment().getTotalFemaleNumbers())).add(BigDecimal.valueOf(line.getPharmacyTreatment().getTotalMaleNumbers()))));
                    }
                    sm.setRealQty(line.getForUseQuantity());
                    sm.setToStockLocation(virtualConsumptionLocation);
                    sm.setProductName(sm.getProduct().getName());
                    sm.setUnit(sm.getProduct().getUnit());
                    stockMove.addStockMoveLineListItem(sm);
                }
            }
        }
        return stockMoveRepo.save(stockMove);
    }

}




