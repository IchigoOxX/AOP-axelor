package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.ElevTransfer;
import com.gl.apps.pf.db.StartReceivingChicks;
import com.gl.apps.pf.db.repo.StartReceivingChicksRepository;
import com.gl.apps.pf.db.repo.StartTechnicalSheetRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


public class StockMoveServiceImp implements StockMoveService{
    @Inject private StockMoveRepository stockMoveRepo;
    @Inject private  StockMoveLineRepository stockMoveLineRepository;
    @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
    @Inject private StartReceivingChicksRepository startReceivingChicksRepository;

    @Transactional
    public StockMove SetValuesLineStockMove(StockMove stockMove) throws AxelorException {
        StockMove stockmov = stockMoveRepo.find(stockMove.getId());
        if (stockmov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND));
        }
        if(!stockmov.getStockMoveLineList().isEmpty()){
            stockmov.getStockMoveLineList().forEach(
                line -> {
                    line.setTrackingNumber(stockmov.getCurrentTrackingNumber());
                });
        }

        return stockMoveRepo.save(stockmov);
    }

    @Transactional
    public StockMove saveTest(Context context) throws AxelorException {
        StockMove stockMouv;

        if(context.get("id") == null) {
            stockMouv = new StockMove();
        }
        else {
            stockMouv = stockMoveRepo.find(Long.valueOf(context.get("id").toString()));
        }
        StockLocation fromStockLoc = (StockLocation) context.get("fromStockLocation");
        Company company = (Company) context.get("company");
        StockLocation toStockLoc = (StockLocation) context.get("toStockLocation");
        Integer typeSelect = (Integer) context.get("typeSelect");
        Integer status = (Integer) context.get("statusSelect");
        Partner partner = (Partner) context.get("partner");
        Partner invoicedPartner = (Partner) context.get("invoicedPartner");
        Address toAddress = (Address) context.get("toAddress");
        LocalDate estimatedDate = (LocalDate) context.get("estimatedDate");

        List<StockMoveLine> list = (List<StockMoveLine>) context.get("stockMoveLineList");

        stockMouv.setFromStockLocation(fromStockLoc);
        stockMouv.setCompany(company);
        stockMouv.setToStockLocation(toStockLoc);
        stockMouv.setTypeSelect(typeSelect);
        stockMouv.setStatusSelect(status);
        stockMouv.setPartner(partner);
        stockMouv.setInvoicedPartner(invoicedPartner);
        stockMouv.setToAddress(toAddress);
        stockMouv.setEstimatedDate(estimatedDate);

        stockMouv.clearStockMoveLineList();

        for (int i = 0; i < list.size(); i++) {
            StockMoveLine stockMoveLine = new StockMoveLine();

            stockMoveLine.setFromStockLocation(list.get(i).getFromStockLocation());
            stockMoveLine.setToStockLocation(list.get(i).getToStockLocation());
            stockMoveLine.setProduct(list.get(i).getProduct());
            stockMoveLine.setProductName(list.get(i).getProductName());
            stockMoveLine.setQty(list.get(i).getQty());
            stockMoveLine.setRealQty(list.get(i).getRealQty());
            stockMoveLine.setUnitPriceUntaxed(list.get(i).getUnitPriceUntaxed());
            stockMoveLine.setTrackingNumber(list.get(i).getTrackingNumber());
            stockMoveLine.setNetMass(list.get(i).getNetMass());

            stockMouv.addStockMoveLineListItem(stockMoveLine);
        }
        return stockMoveRepo.save(stockMouv);
    }

    @Transactional
    public StockMove addChicksReception(StockMove stockmov) throws AxelorException {
        StockMove stockMove = stockMoveRepo.find(stockmov.getId());
        if (stockmov == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND));
        }
        StartReceivingChicks startReceivingChicks = new StartReceivingChicks();
        startReceivingChicks.setDateTime(LocalDateTime.now().toLocalDate());
        startReceivingChicks.setHatchDate(LocalDateTime.now().toLocalDate());
        startReceivingChicks.setSupplierReceptionDate(LocalDateTime.now());
        startReceivingChicks.setSupplierReception(stockMove);
        AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();
        int tFemales = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterFemalesChicks())
                .mapToInt(t -> t.getRealQty().intValue()).sum() : 0;

        int tMales = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterMalesChicks())
                .mapToInt(t -> t.getRealQty().intValue()).sum() : 0;

        int invFemales = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterFemalesChicks())
                .mapToInt(t -> t.getQtyInvoiced().intValue()).sum() : 0;
        int invMales = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterMalesChicks())
                .mapToInt(t -> t.getQtyInvoiced().intValue()).sum() : 0;


        int femaleUnitPrice = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterFemalesChicks())
                .mapToInt(t -> t.getUnitPriceUntaxed().intValue()).sum() : 0;

        int malesUnitPrice = stockMove
                .getStockMoveLineList() != null ? stockMove
                .getStockMoveLineList().stream().filter(t -> t.getProduct() == app.getStarterMalesChicks())
                .mapToInt(t -> t.getUnitPriceUntaxed().intValue()).sum() : 0;

        int costPrice = (tFemales * femaleUnitPrice) + (tMales * malesUnitPrice);
        int amount = (invFemales * femaleUnitPrice) + (invMales * malesUnitPrice);

        startReceivingChicks.setReceivedFemalesNumber(tFemales);
        startReceivingChicks.setReceivedMalesNumber(tMales);
        startReceivingChicks.setReceivedChicksNumber(tFemales + tMales);
        startReceivingChicks.setInvoicedFemalesNumber(invFemales);
        startReceivingChicks.setInvoicedMalesNumber(invMales);
        startReceivingChicks.setInvoicedChicksNumber(invFemales + invMales);

        startReceivingChicks.setCostPrice(costPrice);
        startReceivingChicks.setAmount(amount);

        startReceivingChicks.setBreed(stockMove.getCurrentBreed());
        startReceivingChicks.setTrackingNumber(stockMove.getCurrentTrackingNumber());
        startReceivingChicks.setDailyAge(stockMove.getCurrentDailyAge());
        startReceivingChicks.setBreedingShedActivationDate(LocalDateTime.now());

        startReceivingChicksRepository.save(startReceivingChicks);

        stockMove.setReceivingChicks(startReceivingChicks);

        return stockMoveRepo.save(stockMove);
    }


}
