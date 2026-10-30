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
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.ElevReform;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevReformRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ElevReformServiceImpl implements ElevReformService{
    @Inject private ElevReformRepository elevReformRepository;
    @Inject private StockMoveService stockMoveService;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public ElevReform Validate(ElevReform breedingReform) throws AxelorException {

        ElevReform elevReform = elevReformRepository.find(breedingReform.getId());

        if (elevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }



        BreedingShed b = breedingShedRepository.find(elevReform.getBreedingShed().getId());
        if(elevReform.getFemaleTotalNumbers() > 0) {
            b.setFemalesNumber(elevReform.getFemaleTotalNumbers() - elevReform.getFemaleReformNumber());
        }
        if(elevReform.getMaleTotalNumbers() > 0) {
            b.setMalesNumber(elevReform.getMaleTotalNumbers() - elevReform.getMaleReformNumber());
        }
        breedingShedRepository.save(b);

        StockMove dispatcherMove = CreateOrUpdateStockMove(elevReform);
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
        elevReform.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
        dispatcherMove.setCurrentTrackingNumber(elevReform.getTrackingNumber());
        dispatcherMove.setCurrentDailyAge(elevReform.getDailyAge());
        dispatcherMove.setCurrentBreed(elevReform.getBreed());
        stockMoveService.plan(dispatcherMove);
        elevReform.setStatus(ElevReformRepository.STATUS_EXECUTING);

        return elevReformRepository.save(elevReform);
    }

    public StockMove CreateOrUpdateStockMove(ElevReform breedingReform) throws AxelorException {
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (breedingReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        ElevReform elevReform = elevReformRepository.find(breedingReform.getId());

        if (elevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_SPECIFIED));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();

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

            if(elevReform.getBreedingShed().getStockLocation() != null ) {
                values.put("fromStockLocation", elevReform.getBreedingShed().getStockLocation());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No stock location was specified for breeding shed : " + elevReform.getBreedingShed().getName() + " !"));
            }

            if(elevReform.getSlaughterhouse() != null){
                if(elevReform.getSlaughterhouse().getPhysicalLocation() != null){
                    values.put("toStockLocation", elevReform.getSlaughterhouse().getPhysicalLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.SLAUGHTEHOUE_DEFAULT_LOCATION_NOT_SPECIFIED));

                }

            }
            else if (elevReform.getSlaughterhouse() == null) {

                if(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation() != null) {
                    values.put("toStockLocation", elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation());
                }
                else  throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.PICKUP_DEFAULT_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(elevReform.getCenter());
            stockMove.setElevReform(elevReform);
        }
        else {
            stockMove = elevReform.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if(elevReform !=  null ) {

            if (elevReform.getMaleTotalNumbers() != 0) {
                StockMoveLine sm = new StockMoveLine();
                sm.setProduct(starterMaleChick);
                sm.setQty(BigDecimal.valueOf(elevReform.getMaleReformNumber()));
                sm.setRealQty(BigDecimal.valueOf(elevReform.getMaleReformNumber()));
                sm.setUnitPriceUntaxed(elevReform.getMaleUnitPrice());
                sm.setProductName(sm.getProduct().getName());
                sm.setTrackingNumber(elevReform.getTrackingNumber());
                sm.setUnit(sm.getProduct().getUnit());

                if(elevReform.getBreedingShed().getStockLocation() !=  null) {
                    sm.setFromStockLocation(elevReform.getBreedingShed().getStockLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("No stock location was specified for breeding shed : " + elevReform.getBreedingShed().getName() + " !"));
                }

                if(elevReform.getSlaughterhouse() != null) {
                    if(elevReform.getSlaughterhouse().getPhysicalLocation() != null){
                        sm.setToStockLocation(elevReform.getSlaughterhouse().getPhysicalLocation());
                    }
                    else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_MISSING_FIELD,
                                I18n.get(PoultryFarmingExceptionMessage.SLAUGHTEHOUE_DEFAULT_LOCATION_NOT_SPECIFIED));
                    }

                }
                else if (elevReform.getSlaughterhouse() == null && elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation() != null){
                    sm.setToStockLocation(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.SLAUGHTEHOUE_DEFAULT_LOCATION_NOT_SPECIFIED));
                }

                stockMove.addStockMoveLineListItem(sm);
            }

            if (elevReform.getFemaleTotalNumbers() != 0) {
                StockMoveLine sm1 = new StockMoveLine();
                sm1.setProduct(starterFemaleChick);
                sm1.setQty(BigDecimal.valueOf(elevReform.getFemaleReformNumber()));
                sm1.setRealQty(BigDecimal.valueOf(elevReform.getFemaleReformNumber()));
                sm1.setUnitPriceUntaxed(elevReform.getFemaleUnitPrice());
                sm1.setProductName(sm1.getProduct().getName());
                sm1.setTrackingNumber(elevReform.getTrackingNumber());
                sm1.setUnit(sm1.getProduct().getUnit());

                if(elevReform.getBreedingShed().getStockLocation() != null ){
                    sm1.setFromStockLocation(elevReform.getBreedingShed().getStockLocation());
                }
                else  {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("No stock location was specified for breeding shed : " + elevReform.getBreedingShed().getName() + " !"));
                }

                if(elevReform.getSlaughterhouse() != null){
                    sm1.setToStockLocation(elevReform.getSlaughterhouse().getPhysicalLocation());
                }
                else if (elevReform.getSlaughterhouse() == null && elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation() != null){
                    sm1.setToStockLocation(elevReform.getUnit().getCompany().getStockConfig().getPickupDefaultStockLocation());
                }
                else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get(PoultryFarmingExceptionMessage.SLAUGHTEHOUE_DEFAULT_LOCATION_NOT_SPECIFIED));
                }
                stockMove.addStockMoveLineListItem(sm1);
            }
        }
        return stockMoveRepo.save(stockMove);
    }

    @Transactional
    public ElevReform Cancel(ElevReform breedingReform) throws AxelorException {

        ElevReform elevReform = elevReformRepository.find(breedingReform.getId());

        if (elevReform == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_REFORM_NOT_FOUND));
        }

        StockMove dispatcherMove = elevReform.getDispatchStock();
        if(dispatcherMove != null ){
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);

        }
        BreedingShed b = breedingShedRepository.find(elevReform.getBreedingShed().getId());
        if(elevReform.getFemaleTotalNumbers() > 0) {
            b.setFemalesNumber(elevReform.getFemaleTotalNumbers() + elevReform.getFemaleReformNumber());
        }
        if(elevReform.getMaleTotalNumbers() > 0) {
            b.setMalesNumber(elevReform.getMaleTotalNumbers() + elevReform.getMaleReformNumber());
        }
        breedingShedRepository.save(b);

        elevReform.setStatus(ElevReformRepository.STATUS_CANCELED);

        return elevReformRepository.save(elevReform);
    }
}




