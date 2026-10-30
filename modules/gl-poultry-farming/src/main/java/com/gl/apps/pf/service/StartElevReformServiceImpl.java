package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.Slaughterhouse;
import com.gl.apps.pf.db.StartElevReform;
import com.gl.apps.pf.db.StartReceivingChicks;
import com.gl.apps.pf.db.repo.SlaughterhouseRepository;
import com.gl.apps.pf.db.repo.StartElevReformRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StartElevReformServiceImpl implements StartElevReformService {
    @Inject
    private StartElevReformRepository startElevReformRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;

    @Transactional
    public StartElevReform Validate(StartElevReform startElevReform) throws AxelorException {
        StartElevReform elevReform = startElevReformRepository.find(startElevReform.getId());
        if (elevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        StockMove dispatcherMove = CreateOrUpdateStockMove(elevReform);
        elevReform.setDispatchStock(dispatcherMove);
        stockMoveService.plan(dispatcherMove);

        elevReform.setStatus(StartElevReformRepository.STATUS_EXECUTING);

        return startElevReformRepository.save(elevReform);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(StartElevReform startElevReform)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();

        if (startElevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        StartElevReform elevReform = startElevReformRepository.find(startElevReform.getId());

        if (elevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.START_ELEV_REFORM_NOT_SPECIFIED));
        }

        StockMove stockMove;

        if (elevReform.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(elevReform.getCenter().getUnit().getCompany() != null) {
                values.put("company", elevReform.getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(elevReform.getBreedingShed().getStockLocation() != null ){
                values.put("fromStockLocation", elevReform.getBreedingShed().getStockLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed : " + elevReform.getBreedingShed().getName() + " !"));
            }

            if(elevReform.getSlaughterhouse() != null){
                values.put("toStockLocation", elevReform.getSlaughterhouse().getPhysicalLocation());
            }
            else if (elevReform.getSlaughterhouse() == null){

                if(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation() != null) {
                    values.put("toStockLocation", elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation());
                }
                else  {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.PICKUP_DEFAULT_LOCATION_NOT_SPECIFIED));
                }
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(elevReform.getCenter());
        }
        else {
            stockMove = elevReform.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }

        StockMoveLine sm = new StockMoveLine();

        if (elevReform.getBreedingShed().getReceivedMalesNumber() != 0) {
            if(starterMaleChick != null ){
                sm.setProduct(starterMaleChick);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_PRODUCT_NOT_DEFINED));
            }

        }
        else if (elevReform.getBreedingShed().getReceivedFemalesNumber() != 0) {
            if(starterFemaleChick != null ){
                sm.setProduct(starterFemaleChick);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_PRODUCT_NOT_DEFINED));
            }

        }
        sm.setQty(BigDecimal.valueOf(elevReform.getReformNumber()));
        sm.setRealQty(BigDecimal.valueOf(elevReform.getReformNumber()));
        sm.setUnitPriceTaxed(elevReform.getUnitPrice());
        if(elevReform.getBreedingShed().getStockLocation() != null ){
            sm.setFromStockLocation(elevReform.getBreedingShed().getStockLocation());
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("No stock location was specified for breeding shed : " + elevReform.getBreedingShed().getName() + " !"));
        }

        sm.setProductName(sm.getProduct().getName());
        sm.setUnit(sm.getProduct().getUnit());

        if(elevReform.getSlaughterhouse() != null){
            if(elevReform.getSlaughterhouse().getPhysicalLocation() != null){
                sm.setToStockLocation(elevReform.getSlaughterhouse().getPhysicalLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.SLAUGHTEHOUE_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

        }
        else if (elevReform.getSlaughterhouse() == null){
            if(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation() != null){
                sm.setToStockLocation(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.PICKUP_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
        }
        stockMove.addStockMoveLineListItem(sm);

        return stockMoveRepo.save(stockMove);
    }

}
