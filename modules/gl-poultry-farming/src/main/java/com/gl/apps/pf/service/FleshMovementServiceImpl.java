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
import com.gl.apps.pf.db.FleshMovement;
import com.gl.apps.pf.db.FleshMovementLine;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.db.repo.FleshMovementRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FleshMovementServiceImpl implements FleshMovementService {
    @Inject private FleshMovementRepository fleshMvtRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public FleshMovement Validate(FleshMovement fleshMovement) throws AxelorException {
        FleshMovement movement = fleshMvtRepository.find(fleshMovement.getId());

        if (movement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_MOVEMENT_NOT_FOUND));
        }
        if(!movement.getFleshMovementLineList().isEmpty()){
            movement.getFleshMovementLineList().forEach(
                line -> {
                    line.getToBreedingShed().setTransferDate(movement.getDateTime());
                    line.getToBreedingShed().setTotalReceivedNumbers(movement.getRemainingChicksNumber());

                    BreedingShed b = line.getToBreedingShed();
                    if (b.getReceivedFemalesNumber() > 0) {
                        b.setFemalesNumber(b.getFemalesNumber() + line.getToTransferChicksNumber());
                    }
                }
            );
        }
        int sumOfMovNumber = movement.getFleshMovementLineList().stream()
                .mapToInt(line -> line.getToTransferChicksNumber())
                .sum();

        BreedingShed b1 = movement.getFromBreedingShed();
        if(b1.getReceivedFemalesNumber() > 0) {
            b1.setFemalesNumber(movement.getExistingChicksNumber() - sumOfMovNumber);
        }

        breedingShedRepository.save(b1);
        StockMove dispatcherMove = CreateOrUpdateStockMove(movement);
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
        movement.setDispatchStock(dispatcherMove);
        dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_WORKFORCES);
        dispatcherMove.setRealDate(LocalDate.now());
        stockMoveService.plan(dispatcherMove);
        stockMoveService.realize(dispatcherMove);

        movement.setStatus(ElevTransferRepository.STATUS_EXECUTING);
        return fleshMvtRepository.save(movement);
    }

    @Transactional
    public FleshMovement Cancel(FleshMovement fleshMovement) throws AxelorException {
        FleshMovement movement = fleshMvtRepository.find(fleshMovement.getId());

        if (movement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_MOVEMENT_NOT_FOUND));
        }
        if(!movement.getFleshMovementLineList().isEmpty()){
            movement.getFleshMovementLineList().forEach(
                line -> {
                    BreedingShed b = line.getToBreedingShed();
                    if (b.getReceivedFemalesNumber() > 0) {
                        int tot = b.getFemalesNumber() - line.getToTransferChicksNumber();
                        b.setFemalesNumber(tot);
                    }
                }
            );
        }
        int sumOfMovNumber = movement.getFleshMovementLineList().stream()
                .mapToInt(line -> line.getToTransferChicksNumber())
                .sum();

        BreedingShed b1 = movement.getFromBreedingShed();
        if(b1.getReceivedFemalesNumber() > 0) {
            b1.setFemalesNumber(movement.getFromBreedingShed().getFemalesNumber() + sumOfMovNumber);
        }

        breedingShedRepository.save(b1);

        StockMove dispatcherMove = movement.getDispatchStock();
        if (dispatcherMove != null ) {
            stockMoveService.cancel(dispatcherMove);
            stockMoveService.goBackToDraft(dispatcherMove);
        }

        movement.setStatus(ElevTransferRepository.STATUS_CANCELED);
        return fleshMvtRepository.save(movement);
    }

    @Transactional
    public StockMove CreateOrUpdateStockMove(FleshMovement fleshMovement) throws AxelorException {

        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

        if (fleshMovement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_MOVEMENT_NOT_FOUND));
        }

        FleshMovement movement = fleshMvtRepository.find(fleshMovement.getId());

        if (movement == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_TRANSFER_NOT_SPECIFIED));
        }

        Product chairChick = app.getChairChicks();
        StockLocation chairLocation = app.getChairDefaultLocation();

        StockMove stockMove;

        if (movement.getDispatchStock() == null) {
            Map<String, Object> values = new HashMap<>();
            if(movement.getFromCenter().getUnit().getCompany() != null) {
                values.put("company", movement.getFromCenter().getUnit().getCompany());
            }
            else {
                throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.CENTER_NOT_FOUND));
            }

            values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);

            if(chairLocation == null ){
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.FLESH_DEFAULT_LOCATION_NOT_SPECIFIED));
            }
            else {
                values.put("fromStockLocation", chairLocation);
                values.put("toStockLocation", chairLocation);
            }

            stockMove = stockMoveRepo.create(values);
            stockMove.setFromCenter(movement.getFromCenter());
        }
        else {
            stockMove = movement.getDispatchStock();
            stockMove.clearStockMoveLineList();
        }
        if (!movement.getFleshMovementLineList().isEmpty()) {
            for (FleshMovementLine line : movement.getFleshMovementLineList()) {
                if (line != null) {
                    StockMoveLine sm = new StockMoveLine();
                    if (line.getToTransferChicksNumber() != 0) {
                        if (chairChick != null) {
                            sm.setProduct(chairChick);
                        } else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(PoultryFarmingExceptionMessage.CHAIR_CHICKS_NOT_SPECIFIED));
                        }

                        sm.setQty(BigDecimal.valueOf(line.getToTransferChicksNumber()));
                        sm.setRealQty(BigDecimal.valueOf(line.getToTransferChicksNumber()));
                        if(movement.getFromBreedingShed().getStockLocation() != null ){
                            sm.setFromStockLocation(movement.getFromBreedingShed().getStockLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed : " + movement.getFromBreedingShed().getName() + " !"));
                        }
                        if(line.getToBreedingShed().getStockLocation() != null ){
                            sm.setToStockLocation(line.getToBreedingShed().getStockLocation());
                        }
                        else {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get("No stock location was specified for breeding shed : " + line.getToBreedingShed().getName() + " !"));
                        }

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




