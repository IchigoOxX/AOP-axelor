/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gl.apps.pf.service;

import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.apps.base.db.Product;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.Hatchery;
import com.gl.apps.pf.db.HatcheryEggsReception;
import com.gl.apps.pf.db.ReproProduction;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionRepository;
import com.gl.apps.pf.db.repo.HatcheryRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class HatcheryEggsReceptionServiceImpl implements HatcheryEggsReceptionService {
  @Inject private HatcheryEggsReceptionRepository hatcheryEggsReceptionRepo;
  @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
  @Inject private StockMoveRepository stockMoveRepo;
  @Inject private StockMoveService stockMoveService;
  @Inject private ReproProductionRepository reproProductionRepository;
  @Inject private HatcheryRepository hatcheryRepository;
  @Inject private StockLocationUtilsService stockLocationService;

  @Transactional
  public HatcheryEggsReception Validate(HatcheryEggsReception hatcheryEggsReception)
          throws AxelorException {
    HatcheryEggsReception reception = hatcheryEggsReceptionRepo.find(hatcheryEggsReception.getId());

    if (reception == null) {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              hatcheryEggsReception.getReference());
    }
    //update production : status = 2 , arrivaltime = now
    //create stock mouvement
    if (!reception.getHatcheryEggsReceptionLineList().isEmpty()){
      reception.getHatcheryEggsReceptionLineList().forEach(
              line -> {
                if (line != null) {
                    line.getBreedingShed().setDailyAge(reception.getDailyAge());
                    line.getBreedingShed().setWeeklyAge(reception.getWeeklyAge());
                }
              });
    }
    if (reception.getStatus() != HatcheryEggsReceptionRepository.STATUS_WAITING) {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.MISSING_STATUS),
              hatcheryEggsReception.getReference());
    }

    ReproProduction production = reproProductionRepository.find(reception.getReproProduction().getId());
    if (production == null) {
      throw new AxelorException(
              reception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              reception.getReference());
    }

    production.setStatus(ReproProductionRepository.STATUS_EXECUTED);
    production.setArrivalTime(reception.getArrivalTime());

    StockMove sm = CreateOrUpdateStockMove(reception);
    for (StockMoveLine line : sm.getStockMoveLineList()) {
      BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(sm.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                    if (!line.getProduct().getAllowNegativeStock()) {
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + sm.getFromStockLocation().getName() + " !")
                            );
                        }
                    }
    }
    reception.setStatus(HatcheryEggsReceptionRepository.STATUS_EXECUTING);
    sm.setSubType(StockMoveRepository.SUB_TYPE_HATCHERY_EGGS_RECEPTION);
    production.setStockMove(sm);
    reception.setStockMove(sm);
    production = reproProductionRepository.save(production);
    return hatcheryEggsReceptionRepo.save(reception);

  }

  @Transactional
  public HatcheryEggsReception ConfirmStock(HatcheryEggsReception hatcheryEggsReception)
          throws AxelorException {
    HatcheryEggsReception reception = hatcheryEggsReceptionRepo.find(hatcheryEggsReception.getId());

    if (reception == null) {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              hatcheryEggsReception.getReference());
    }

    if (reception.getStatus() != HatcheryEggsReceptionRepository.STATUS_EXECUTING) {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.MISSING_STATUS),
              hatcheryEggsReception.getReference());
    }

    StockMove stockMove;

    if (reception.getStockMove() != null) {
      stockMove = stockMoveRepo.find(reception.getStockMove().getId());
    }
    else {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND),
              hatcheryEggsReception.getReference());
    }

    stockMoveService.plan(stockMove);
    stockMove = stockMoveRepo.find(reception.getStockMove().getId());
    stockMoveService.realize(stockMove, true);
    stockMove.setRealDate(LocalDate.now());
    reception.setStatus(HatcheryEggsReceptionRepository.STATUS_EXECUTED);

    return hatcheryEggsReceptionRepo.save(reception);

  }

  @Transactional
  public StockMove CreateOrUpdateStockMove(HatcheryEggsReception reception)
          throws AxelorException {

    AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

    if (reception == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_RECEPTION_NOT_FOUND));
    }

//    if (reception.getStatus() != HatcheryEggsReceptionRepository.STATUS_WAITING) {
//      throw new AxelorException(
//              reception,
//              TraceBackRepository.CATEGORY_MISSING_FIELD,
//              I18n.get(PoultryFarmingExceptionMessage.MISSING_STATUS),
//              reception.getReference());
//    }

    Hatchery hatchery = hatcheryRepository.find(reception.getHatchery().getId());
    if (hatchery == null) {
      throw new AxelorException(
              reception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED),
              reception.getReference());
    }

    Product hatchingEggProduct = app.getHatchingEggProduct();
    Product doubleYolkEggProduct = app.getDoubleYolkEggProduct();
    Product declassedEggsProduct = app.getDeclassedEggsProduct();
    StockLocation eggsDefaultBreedingLocation = app.getEggsDefaultBreedingLocation();
    StockLocation hatcheryEggDefaultLocation = app.getHatchryEggDefaultLocation();

    StockMove stockMove;

    if (reception.getStockMove() == null) {
      Map<String, Object> values = new HashMap<>();
      if(hatchery.getUnit().getCompany() != null ){
        values.put("company", hatchery.getUnit().getCompany());
      }
      else {
        throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.HATCHERY_COMPANY_NOT_FOUND));
      }

      values.put("typeSelect", StockMoveRepository.TYPE_INTERNAL);
      if(eggsDefaultBreedingLocation != null ){
        values.put("fromStockLocation", eggsDefaultBreedingLocation);
      }
      else {
        throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.EGG_DEFAULT_BREEDING_LOCATION_NOT_SPECIFIED));

      }
      if(hatcheryEggDefaultLocation != null) {
        values.put("toStockLocation", hatcheryEggDefaultLocation);
      }
      else {
        throw new AxelorException(
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
      }

      stockMove = stockMoveRepo.create(values);
      stockMove.setFromCenter(reception.getCenter());
      stockMove.setHatcheryEggsReception(reception);

    } else {
      stockMove = reception.getStockMove();
      stockMove.clearStockMoveLineList();
    }

    List<TempStockMoveLine> lines = new ArrayList<>();

    // Oeufs à couver (OAC)
    if (reception.getSumNumberOfHe() != 0 || reception.getSumReceivedNumberOfHe() != 0) {
      if (hatchingEggProduct == null) {
        throw new AxelorException(
                reception,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(PoultryFarmingExceptionMessage.HATCHING_EGG_PRODUCT_NOT_DEFINED),
                reception.getReference());
      }

      TempStockMoveLine line = new TempStockMoveLine();
      line.setProduct(hatchingEggProduct);
      line.setQty(BigDecimal.valueOf(reception.getSumNumberOfHe()));
      line.setRealQty(BigDecimal.valueOf(reception.getSumReceivedNumberOfHe()));
      lines.add(line);
    }

    //Oeufs double jaune (ODJ)
    if (reception.getSumNumberOfDye() != 0 || reception.getSumReceivedNumberOfDye() != 0) {
      if (doubleYolkEggProduct == null) {
        throw new AxelorException(
                reception,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(PoultryFarmingExceptionMessage.DOUBLE_YOLK_EGG_PRODUCT_NOT_DEFINED),
                reception.getReference());
      }
      TempStockMoveLine line = new TempStockMoveLine();
      line.setProduct(doubleYolkEggProduct);
      line.setQty(BigDecimal.valueOf(reception.getSumNumberOfDye()));
      line.setRealQty(BigDecimal.valueOf(reception.getSumReceivedNumberOfDye()));
      lines.add(line);
    }

    //Oeufs déclassés (OD)
    if (reception.getSumNumberOfDe() != 0 || reception.getSumReceivedNumberOfDe() != 0) {
      if (declassedEggsProduct == null) {
        throw new AxelorException(
                reception,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(PoultryFarmingExceptionMessage.DECLASSED_EGG_PRODUCT_NOT_DEFINED),
                reception.getReference());
      }
      TempStockMoveLine line = new TempStockMoveLine();
      line.setProduct(declassedEggsProduct);
      line.setQty(BigDecimal.valueOf(reception.getSumNumberOfDe()));
      line.setRealQty(BigDecimal.valueOf(reception.getSumReceivedNumberOfDe()));
      lines.add(line);
    }

    lines.forEach(
            line -> {
              StockMoveLine sm = new StockMoveLine();
              sm.setFromStockLocation(stockMove.getFromStockLocation());
              sm.setToStockLocation(stockMove.getToStockLocation());
              sm.setProduct(line.getProduct());
              sm.setProductName(line.getProduct().getName());
              sm.setUnit(line.getProduct().getUnit());
              sm.setQty(line.getQty());
              sm.setRealQty(line.getRealQty());
              stockMove.addStockMoveLineListItem(sm);
            });

    return stockMoveRepo.save(stockMove);
  }

  @Transactional
  public HatcheryEggsReception Cancel(HatcheryEggsReception hatcheryEggsReception)
          throws AxelorException {
    HatcheryEggsReception reception = hatcheryEggsReceptionRepo.find(hatcheryEggsReception.getId());

    if (reception == null) {
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              hatcheryEggsReception.getReference());
    }

    StockMove stockMove;

    if (reception.getStockMove() != null) {
      stockMove = stockMoveRepo.find(reception.getStockMove().getId());
    }
    else{
      throw new AxelorException(
              hatcheryEggsReception,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.STOCK_MOVE_NOT_FOUND),
              hatcheryEggsReception.getReference());
    }
    stockMoveService.cancel(stockMove);
    stockMoveService.goBackToDraft(stockMove);

    reception.setStatus(HatcheryEggsReceptionRepository.STATUS_EXECUTING);
    return hatcheryEggsReceptionRepo.save(reception);

  }

  private class TempStockMoveLine {
    private Product product;
    BigDecimal qty;
    BigDecimal realQty;

    public Product getProduct() {
      return product;
    }

    public void setProduct(Product product) {
      this.product = product;
    }

    public BigDecimal getQty() {
      return qty == null ? BigDecimal.ZERO : qty;
    }

    public void setQty(BigDecimal qty) {
      this.qty = qty;
    }

    public BigDecimal getRealQty() {
      return realQty == null ? BigDecimal.ZERO : realQty;
    }

    public void setRealQty(BigDecimal realQty) {
      this.realQty = realQty;
    }
  }


}




