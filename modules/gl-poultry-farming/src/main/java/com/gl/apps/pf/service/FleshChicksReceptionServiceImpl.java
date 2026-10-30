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
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.FleshChicksReception;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class FleshChicksReceptionServiceImpl implements FleshChicksReceptionService {
    @Inject private FleshChicksReceptionRepository fleshChicksReceptionRepository;
    @Inject private BreedingShedRepository breedingShedRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private StockMoveService stockMoveService;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public FleshChicksReception Validate(FleshChicksReception fleshChicksReception) throws AxelorException {
        FleshChicksReception chicksReception = fleshChicksReceptionRepository.find(fleshChicksReception.getId());

        if (chicksReception == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_NOT_FOUND));
        }
        if (chicksReception.getCenter() != null ) {
            chicksReception.getCenter().setCurrentBreed(chicksReception.getBreed());
            chicksReception.getCenter().setCurrentTrackingNumber(chicksReception.getTrackingNumber());
            chicksReception.getCenter().setCompleteRefTrackingNumber(chicksReception.getTrackingNumber().getTrackingNumberSeq() + " - " + chicksReception.getHatcherExit().getHatcherExitLinesList().get(0).getTrackingNumber().getTrackingNumberSeq());
            chicksReception.getCenter().setCurrentDailyAge(1);

            if(chicksReception.getBreedingShed() != null) {
                chicksReception.getBreedingShed().setBreed(chicksReception.getBreed());
                chicksReception.getBreedingShed().setReceivedFemalesNumber(chicksReception.getNumberOfChicksReceived());
                chicksReception.getBreedingShed().setFemalesNumber(chicksReception.getNumberOfChicksReceived());
                chicksReception.getBreedingShed().setCurrentBatchNumber(chicksReception.getTrackingNumber());
                chicksReception.getBreedingShed().setCompleteRefTrackingNumber(chicksReception.getTrackingNumber().getTrackingNumberSeq() + " - " + chicksReception.getHatcherExit().getHatcherExitLinesList().get(0).getTrackingNumber().getTrackingNumberSeq());
                chicksReception.getBreedingShed().setStatus(BreedingShedRepository.STATUS_BUSY);
                chicksReception.getBreedingShed().setFillingDate(chicksReception.getCreationDate());
                chicksReception.getBreedingShed().setDailyAge(1);
            }
            else throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));

        }
        else throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.CENTER_NOT_FOUND));

        if(chicksReception.getHatcherExit() != null ) {
            chicksReception.getHatcherExit().setStatus(HatcherExitRepository.STATUS_EXECUTED);
        }
        else throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));

        String completeRefTrackingNumber = chicksReception.getTrackingNumber().getTrackingNumberSeq() + " - " + chicksReception.getHatcherExit().getHatcherExitLinesList().get(0).getTrackingNumber().getTrackingNumberSeq();
        chicksReception.setCompleteRefTrackingNumber(completeRefTrackingNumber);

        if(chicksReception.getFleshLoadingProgramLine() != null) {
            chicksReception.getFleshLoadingProgramLine().setStatus(FleshLoadingProgramLineRepository.STATUS_EXECUTING);

            StockMove receptionStockMove = chicksReception.getFleshLoadingProgramLine().getFleshReceptionDispatchStock();
            if (receptionStockMove != null ){
                for (StockMoveLine line : receptionStockMove.getStockMoveLineList()) {
                    BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(receptionStockMove.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                    if (!line.getProduct().getAllowNegativeStock()) {
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + receptionStockMove.getFromStockLocation().getName() + " !")
                            );
                        }
                    }
                }
                receptionStockMove.setSubType(StockMoveRepository.SUB_TYPE_FLESH_CHICKS_RECEPTION);
                receptionStockMove.setFleshChicksReception(chicksReception);
                receptionStockMove.setRealDate(LocalDate.now());
                stockMoveService.plan(receptionStockMove);
                stockMoveService.realize(receptionStockMove);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_DISPATCH_STOCK_NOT_FOUND));
            }
        }
        else throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));

        StockMove dispatcherMove = chicksReception.getHatcherExit().getDispatchStock();
            if(dispatcherMove != null) {
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
                dispatcherMove.setFleshChicksReception(chicksReception);
                dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_FLESH_CHICKS_RECEPTION);
                dispatcherMove.setRealDate(LocalDate.now());
                stockMoveService.realize(dispatcherMove);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_DISPATCH_STOCK_NOT_FOUND));
            }

        chicksReception.setStatus(FleshChicksReceptionRepository.STATUS_EXECUTING);
        return fleshChicksReceptionRepository.save(chicksReception);

    }
    @Transactional
    public FleshChicksReception Cancel(FleshChicksReception fleshChicksReception) throws AxelorException {
        FleshChicksReception chicksReception = fleshChicksReceptionRepository.find(fleshChicksReception.getId());

        if (chicksReception == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_NOT_FOUND));
        }
        if(chicksReception.getBreedingShed() != null) {
            chicksReception.getBreedingShed().setBreed(null);
            chicksReception.getBreedingShed().setReceivedFemalesNumber(0);
            chicksReception.getBreedingShed().setFemalesNumber(0);
            chicksReception.getBreedingShed().setCurrentBatchNumber(null);
            chicksReception.getBreedingShed().setCompleteRefTrackingNumber(null);
            chicksReception.getBreedingShed().setStatus(BreedingShedRepository.STATUS_ACTIVATED);
            chicksReception.getBreedingShed().setFillingDate(null);
            chicksReception.getBreedingShed().setDailyAge(0);
        }
            else throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));

        if(chicksReception.getHatcherExit() != null ) {
            chicksReception.getHatcherExit().setStatus(HatcherExitRepository.STATUS_CANCELED);
        }
        else throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_NOT_FOUND));

        chicksReception.setCompleteRefTrackingNumber(null);

        if(chicksReception.getFleshLoadingProgramLine() != null) {
            chicksReception.getFleshLoadingProgramLine().setStatus(FleshLoadingProgramLineRepository.STATUS_NOT_EXECUTED);

            StockMove receptionStockMove = chicksReception.getFleshLoadingProgramLine().getFleshReceptionDispatchStock();
            if (receptionStockMove != null ){
                stockMoveService.cancel(receptionStockMove);
                stockMoveService.goBackToDraft(receptionStockMove);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.FLESH_CHICKS_RECEPTION_DISPATCH_STOCK_NOT_FOUND));
            }
        }
        else throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));

        StockMove dispatcherMove = chicksReception.getHatcherExit().getDispatchStock();
        if(dispatcherMove != null) {
            stockMoveService.plan(dispatcherMove);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_EXIT_DISPATCH_STOCK_NOT_FOUND));
        }

        chicksReception.setStatus(FleshChicksReceptionRepository.STATUS_CANCELED);
        return fleshChicksReceptionRepository.save(chicksReception);

    }




}




