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
import com.gl.apps.pf.db.ReproElevMovement;
import com.gl.apps.pf.db.ReproElevMovementLine;
import com.gl.apps.pf.db.StartElevMovement;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.db.repo.ReproElevMovementRepository;
import com.gl.apps.pf.db.repo.StartElevMovementRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class ReproElevMovementServiceImpl implements  ReproElevMovementService{
    @Inject
    private ReproElevMovementRepository reproElevMovementRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private StockLocationUtilsService stockLocationService;

    @Transactional
    public ReproElevMovement Validate(ReproElevMovement reproElevMovement) throws AxelorException {
        ReproElevMovement elevMov = reproElevMovementRepository.find(reproElevMovement.getId());
        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_MOVEMENT_NOT_FOUND));
        }
        StockMove stockMove;
        if(!elevMov.getReproElevMovementLineList().isEmpty()){
            elevMov.getReproElevMovementLineList().forEach(
                line -> {
                    line.getBreedingShed().setDisplacementDate(elevMov.getDateTime());
                    BreedingShed b = line.getBreedingShed();
                    b.setFemalesNumber(b.getFemalesNumber() + line.getFemaleMovedNumber());
                    b.setMalesNumber(b.getMalesNumber() + line.getMaleMovedNumber());
                }
            );
        }

        int sumOfMaleMovNumber = elevMov.getReproElevMovementLineList().stream()
                .mapToInt(line -> line.getMaleMovedNumber())
                .sum();
        int sumOfFemaleMovNumber = elevMov.getReproElevMovementLineList().stream()
                .mapToInt(line -> line.getFemaleMovedNumber())
                .sum();

        BreedingShed b1 = elevMov.getBreedingShed();
            b1.setFemalesNumber(elevMov.getFemaleTotalNumbers() - sumOfFemaleMovNumber);
            b1.setMalesNumber(elevMov.getMaleTotalNumbers() - sumOfMaleMovNumber);

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = CreateOrUpdateStockMove(elevMov);
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
        elevMov.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);
        dispatcherMove.setRealDate(LocalDate.now());

        elevMov.setStatus(ReproElevMovementRepository.STATUS_EXECUTING);
        return reproElevMovementRepository.save(elevMov);

    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(ReproElevMovement reproElevMovement)
            throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (reproElevMovement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_MOVEMENT_NOT_FOUND));
        }

        ReproElevMovement elevMov = reproElevMovementRepository.find(reproElevMovement.getId());

        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_MOVEMENT_NOT_SPECIFIED));
        }

        Product starterFemaleChick = app.getStarterFemalesChicks();
        Product starterMaleChick = app.getStarterMalesChicks();
        StockLocation virtualReproLocation = app.getVirtualProductionLocation();

        StockMove stockMove;

        if (elevMov.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(elevMov.getCenter().getUnit().getCompany() != null) {
                values.put("company", elevMov.getCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.COMPANY_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
            if(virtualReproLocation != null) {
                values.put("fromStockLocation", virtualReproLocation);
                values.put("toStockLocation", virtualReproLocation);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.VIRTUAL_PRODUCTION_LOCATION_NOT_SPECIFIED));
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(elevMov.getCenter());
        }
        else {
            stockMove = elevMov.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!elevMov.getReproElevMovementLineList().isEmpty()) {
            for (ReproElevMovementLine line : elevMov.getReproElevMovementLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getBreedingShed().getFemalesNumber() != 0) {
                        if (starterFemaleChick != null) {
                            sm.setProduct(starterFemaleChick);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STARTER_FEMALE_PRODUCT_NOT_DEFINED));
                        }

                        sm.setQty(BigDecimal.valueOf(line.getFemaleMovedNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getFemaleMovedNumber()));

                    } else if (line.getBreedingShed().getMalesNumber() != 0) {
                        if(starterMaleChick != null){
                            sm.setProduct(starterMaleChick);
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.STARTER_MALE_PRODUCT_NOT_DEFINED));
                        }

                        sm.setQty(BigDecimal.valueOf(line.getMaleMovedNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getMaleMovedNumber()));
                    }
                    if(elevMov.getBreedingShed().getStockLocation() != null ){
                        sm.setFromStockLocation(elevMov.getBreedingShed().getStockLocation());
                    }
                    else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_MISSING_FIELD,
                                I18n.get("No stock location was specified for breeding shed : " + elevMov.getBreedingShed().getName() + " !"));
                    }
                    if(line.getBreedingShed().getStockLocation() != null){
                        sm.setToStockLocation(line.getBreedingShed().getStockLocation());
                    }
                    else {
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_MISSING_FIELD,
                                I18n.get("No stock location was specified for breeding shed : " + line.getBreedingShed().getName() + " !"));
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
    public ReproElevMovement Cancel(ReproElevMovement reproElevMovement) throws AxelorException {
        ReproElevMovement elevMov = reproElevMovementRepository.find(reproElevMovement.getId());
        if (elevMov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.REPRO_ELEV_MOVEMENT_NOT_FOUND));
        }
        StockMove stockMove;
        if(!elevMov.getReproElevMovementLineList().isEmpty()){
            elevMov.getReproElevMovementLineList().forEach(
                    line -> {
                        line.getBreedingShed().setDisplacementDate(elevMov.getDateTime());
                        BreedingShed b = line.getBreedingShed();
                        b.setFemalesNumber(b.getFemalesNumber() - line.getFemaleMovedNumber());
                        b.setMalesNumber(b.getMalesNumber() - line.getMaleMovedNumber());
                    }
            );
        }

        int sumOfMaleMovNumber = elevMov.getReproElevMovementLineList().stream()
                .mapToInt(line -> line.getMaleMovedNumber())
                .sum();
        int sumOfFemaleMovNumber = elevMov.getReproElevMovementLineList().stream()
                .mapToInt(line -> line.getFemaleMovedNumber())
                .sum();

        BreedingShed b1 = elevMov.getBreedingShed();
        b1.setFemalesNumber(b1.getFemalesNumber() + sumOfFemaleMovNumber);
        b1.setMalesNumber(b1.getMalesNumber() + sumOfMaleMovNumber);

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = elevMov.getDispatchStock();
        if(dispatcherMove != null) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        elevMov.setStatus(ReproElevMovementRepository.STATUS_CANCELED);
        return reproElevMovementRepository.save(elevMov);

    }

}




