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

import com.axelor.apps.base.db.Product;
//import com.axelor.apps.base.db.repo.AppPoultryfarmingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppPoultryFarming;
import com.axelor.studio.db.repo.AppPoultryFarmingRepository;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionRepository;
import com.gl.apps.pf.db.repo.HatcheryRepository;
import com.gl.apps.pf.db.repo.ReproProductionRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.eclipse.birt.chart.extension.render.Stock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReproProductionServiceImpl implements ReproProductionService {
  @Inject private ReproProductionRepository reproProductionRepository;
  @Inject private StockMoveRepository stockMoveRepo;
  @Inject private HatcheryRepository hatcheryRepository;
  @Inject private HatcheryEggsReceptionRepository hatcheryEggsReceptionRepository;
  @Inject private AppPoultryFarmingRepository appPoultryfarmingRepository;
  @Inject private StockMoveService stockMoveService;
  @Inject private StockLocationUtilsService stockLocationService;

  @Transactional
  public ReproProduction Validate(ReproProduction reproProduction) throws AxelorException {
    ReproProduction production = reproProductionRepository.find(reproProduction.getId());
    if (production == null) {
      throw new AxelorException(
              reproProduction,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              reproProduction.getReference());
    }

    HatcheryEggsReception hatcheryEggsReception = new HatcheryEggsReception();
    hatcheryEggsReception.setReproProduction(production);
    hatcheryEggsReception.setHatchery(production.getHatchery());
    hatcheryEggsReception.setDateOfOrigin(production.getDateTime());
    hatcheryEggsReception.setDateTime(production.getDateTime());
    hatcheryEggsReception.setUnit(production.getUnit());
    hatcheryEggsReception.setCenter(production.getCenter());
    hatcheryEggsReception.setDailyAge(production.getDailyAge());
    hatcheryEggsReception.setWeeklyAge(production.getWeeklyAge());
    hatcheryEggsReception.setTrackingNumber(production.getTrackingNumber());
    hatcheryEggsReception.setBreed(production.getBreed());

    if (!production.getReproProductionLineList().isEmpty()) {
      hatcheryEggsReception.clearHatcheryEggsReceptionLineList();
      production.getReproProductionLineList()
          .forEach(
              line -> {
                HatcheryEggsReceptionLine rline = new HatcheryEggsReceptionLine();
                rline.setBreedingShed(line.getBreedingShed());
                rline.setLineNumber(line.getLineNumber().toString());
                rline.setNumberOfHeTrays(line.getNumberOfHeTrays());
                rline.setNumberOfHe(line.getNumberOfHe());
                rline.setNumberOfDeTrays(line.getNumberOfDETrays());
                rline.setNumberOfDyeTrays(line.getNumberOfDYETrays());
                rline.setNumberOfDe(line.getNumberOfDE());
                rline.setNumberOfDye(line.getNumberOfDYE());
                rline.setNumberOfRe(line.getNumberOfRE());
                hatcheryEggsReception.addHatcheryEggsReceptionLineListItem(rline);
              });
    }
    hatcheryEggsReception.setStatus(HatcheryEggsReceptionRepository.STATUS_WAITING);
    hatcheryEggsReceptionRepository.save(hatcheryEggsReception);


    StockMove dispatcherMove = CreateOrUpdateStockMove(production);
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
    production.setStockMove(dispatcherMove);
    dispatcherMove.setSubType(StockMoveRepository.SUB_TYPE_REPRO_PRODUCTION);
    stockMoveService.plan(dispatcherMove);
    stockMoveService.realize(dispatcherMove, true);
    dispatcherMove.setRealDate(LocalDate.now());
    production.setStatus(ReproProductionRepository.STATUS_EXECUTING);

    return reproProductionRepository.save(production);

  }

  @Transactional
  public StockMove CreateOrUpdateStockMove(ReproProduction reproProduction)
          throws AxelorException {

    AppPoultryFarming app = appPoultryfarmingRepository.all().fetchOne();

    if (reproProduction == null) {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND));
    }

    Hatchery hatchery = hatcheryRepository.find(reproProduction.getHatchery().getId());
    if (hatchery == null) {
      throw new AxelorException(
              reproProduction,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED),
              reproProduction.getReference());
    }

    Product hatchingEggProduct = app.getHatchingEggProduct();
    Product doubleYolkEggProduct = app.getDoubleYolkEggProduct();
    Product declassedEggsProduct = app.getDeclassedEggsProduct();
    Product rejectedEggsProduct = app.getRejectedEggsProduct();
    Product detroyedEggsProduct = app.getDestroyedEggsProduct();
    Product unfertilizedEggs = app.getUnfertilizedEggsProduct();
    StockLocation eggsDefaultBreedingLocation = app.getEggsDefaultBreedingLocation();
    StockLocation hatchryEggDefaultLocation = app.getHatchryEggDefaultLocation();

    Map<String, Object> values = new HashMap<>();
    if(hatchery.getUnit().getCompany() != null) {
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
    if(hatchryEggDefaultLocation != null ) {
      values.put("toStockLocation", hatchryEggDefaultLocation);
    }
    else {
      throw new AxelorException(
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.HATCHERY_EGG_DEFAULT_LOCATION_NOT_SPECIFIED));
    }

    StockMove stockMove = stockMoveRepo.create(values);
    stockMove.setReproProduction(reproProduction);

    List<TempStockMoveLine> lines = new ArrayList<>();

    if (!reproProduction.getReproProductionLineList().isEmpty()) {
      for (ReproProductionLine lineRepro : reproProduction.getReproProductionLineList()) {
        if (lineRepro != null) {
          if (lineRepro.getTotalNumberOfHe() != 0) {
            TempStockMoveLine line = new TempStockMoveLine();
            if (hatchingEggProduct != null) {
              line.setProduct(hatchingEggProduct);
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.HATCHING_EGG_PRODUCT_NOT_DEFINED));
            }
            line.setQty(BigDecimal.valueOf(lineRepro.getTotalNumberOfHe()));
            line.setRealQty(BigDecimal.valueOf(lineRepro.getTotalNumberOfHe()));
            lines.add(line);
          }

          if (lineRepro.getTotalNumberOfDE() != 0) {
            TempStockMoveLine line = new TempStockMoveLine();
            if(declassedEggsProduct != null ){
              line.setProduct(declassedEggsProduct);
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.DECLASSED_EGG_PRODUCT_NOT_DEFINED));
            }
            line.setQty(BigDecimal.valueOf(lineRepro.getNumberOfDE() + lineRepro.getNumberOfDEGround()));
            line.setRealQty(BigDecimal.valueOf(lineRepro.getNumberOfDE() + lineRepro.getNumberOfDEGround()));
            lines.add(line);
          }

          if (lineRepro.getNumberOfDYE() != 0) {
            TempStockMoveLine line = new TempStockMoveLine();
            if(doubleYolkEggProduct != null ){
              line.setProduct(doubleYolkEggProduct);
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.DECLASSED_EGG_PRODUCT_NOT_DEFINED));
            }

            line.setQty(BigDecimal.valueOf(lineRepro.getNumberOfDYE()));
            line.setRealQty(BigDecimal.valueOf(lineRepro.getNumberOfDYE()));
            lines.add(line);
          }

          if (lineRepro.getNumberOfRE() != 0) {
            TempStockMoveLine line = new TempStockMoveLine();
            if(rejectedEggsProduct != null ) {
              line.setProduct(rejectedEggsProduct);
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.REJECTED_EGG_PRODUCT_NOT_DEFINED));
            }

            line.setQty(BigDecimal.valueOf(lineRepro.getNumberOfRE()));
            line.setRealQty(BigDecimal.valueOf(lineRepro.getNumberOfRE()));
            lines.add(line);
          }
          if (lineRepro.getNumberOfDestroyedRe() != 0) {
            TempStockMoveLine line = new TempStockMoveLine();
            if(detroyedEggsProduct != null ){
              line.setProduct(detroyedEggsProduct);
            } else {
              throw new AxelorException(
                      TraceBackRepository.CATEGORY_MISSING_FIELD,
                      I18n.get(PoultryFarmingExceptionMessage.DESTROYED_EGG_PRODUCT_NOT_DEFINED));
            }

            line.setQty(BigDecimal.valueOf(lineRepro.getNumberOfDestroyedRe()));
            line.setRealQty(BigDecimal.valueOf(lineRepro.getNumberOfDestroyedRe()));
            lines.add(line);
          }
//          if (lineRepro.getUnfertilizedEggs() != 0) {
//            TempStockMoveLine line = new TempStockMoveLine();
//            if(unfertilizedEggs != null ){
//              line.setProduct(unfertilizedEggs);
//            } else {
//              throw new AxelorException(
//                      TraceBackRepository.CATEGORY_MISSING_FIELD,
//                      I18n.get(PoultryFarmingExceptionMessage.UNFERTILIZED_EGG_PRODUCT_NOT_DEFINED));
//            }
//
//            line.setQty(BigDecimal.valueOf(lineRepro.getUnfertilizedEggs()));
//            line.setRealQty(BigDecimal.valueOf(lineRepro.getUnfertilizedEggs()));
//            lines.add(line);
//          }
        }
      }

      lines.forEach(line -> {
          StockMoveLine sm = new StockMoveLine();

          sm.setProduct(line.getProduct());
          sm.setProductName(line.getProduct().getName());
          sm.setUnit(line.getProduct().getUnit());
          sm.setQty(line.getQty());
          sm.setRealQty(line.getRealQty());
          line.setTrackingNumber(reproProduction.getTrackingNumber());
          sm.setFromStockLocation(eggsDefaultBreedingLocation);
          sm.setToStockLocation(hatchryEggDefaultLocation);
          stockMove.addStockMoveLineListItem(sm);
        });
      }

    return stockMoveRepo.save(stockMove);
  }
  @Transactional
  public ReproProduction Cancel(ReproProduction reproProduction) throws AxelorException {
    ReproProduction production = reproProductionRepository.find(reproProduction.getId());
    if (production == null) {
      throw new AxelorException(
              reproProduction,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(PoultryFarmingExceptionMessage.EGG_PRODUCTION_NOT_FOUND),
              reproProduction.getReference());
    }

    StockMove dispatcherMove = production.getStockMove();
    if(dispatcherMove != null) {
      stockMoveService.cancel(dispatcherMove);
      stockMoveService.goBackToDraft(dispatcherMove);
    }
    production.setStatus(ReproProductionRepository.STATUS_CANCELED);

    return reproProductionRepository.save(production);

  }


  private class TempStockMoveLine {
    private Product product;
    BigDecimal qty;
    BigDecimal realQty;
    StockLocation fromStockLocation;
    StockLocation toStockLocation;
    TrackingNumber trackingNumber;

    public Product getProduct() {
      return product;
    }

    public void setProduct(Product product) {
      this.product = product;
    }

    public BigDecimal getQty() {
      return qty == null ? BigDecimal.ZERO : qty;
    }

    public StockLocation getToStockLocation() {
      return toStockLocation;
    }

    public TrackingNumber getTrackingNumber() {
      return trackingNumber;
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
    public void setFromStockLocation(StockLocation fromStockLocation) {
      this.fromStockLocation = fromStockLocation;
    }
    public void setToStockLocation(StockLocation toStockLocation) {
      this.toStockLocation = toStockLocation;
    }

    public void setTrackingNumber(TrackingNumber trackingNumber) {
      this.trackingNumber = trackingNumber;
    }
  }
}




