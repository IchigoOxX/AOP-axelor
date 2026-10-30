package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.LivestockFeedProduction;
import com.gl.apps.pf.db.LivestockFeedProductionLine;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.LivestockFeedProductionService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class LiveStockFeedProductionController {
    @Inject private LivestockFeedProductionService livestockFeedProductionService;
    @Inject private BillOfMaterialRepository billOfMaterialRepository;

    public void getLinesFromProduct(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedProduction production = request.getContext().asType(LivestockFeedProduction.class);
        if (production != null && production.getProduct() != null) {
            production.clearLivestockFeedProductionLineList();

            Product prod = production.getProduct();
            BillOfMaterial bom = billOfMaterialRepository.all()
                    .filter("self.product = ?1", prod)
                    .fetchOne();

            if (bom == null && prod.getDefaultBillOfMaterial() != null) {
                bom = prod.getDefaultBillOfMaterial();
            }

            if (bom != null && bom.getBillOfMaterialLineList() != null) {
                for (BillOfMaterialLine line : bom.getBillOfMaterialLineList()) {
                    if (line != null && line.getProduct() != null) {
                        LivestockFeedProductionLine pLine = new LivestockFeedProductionLine();
                        pLine.setLivestockFeedProduction(production);
                        pLine.setProduct(line.getProduct());
                        pLine.setCodeProduct(line.getProduct().getCode() != null ? line.getProduct().getCode() : line.getProduct().getName());
                        String unitName = line.getUnit() != null ? line.getUnit().getName() : (line.getProduct().getUnit() != null ? line.getProduct().getUnit().getName() : "Kg");
                        pLine.setUmProduct(unitName);
                        BigDecimal comp = line.getQty() != null ? line.getQty() : BigDecimal.ZERO;
                        pLine.setCompositionProduct(comp);
                        BigDecimal price = line.getProduct().getCostPrice() != null ? line.getProduct().getCostPrice() : (line.getProduct().getPurchasePrice() != null ? line.getProduct().getPurchasePrice() : BigDecimal.ZERO);
                        pLine.setUnitPriceProduct(price);
                        if (production.getQuantityProduced() != null) {
                            BigDecimal qty = comp.divide(new BigDecimal(100)).multiply(production.getQuantityProduced());
                            pLine.setQuantityProduct(qty);
                            pLine.setTotalAmount(qty.multiply(price));
                        } else {
                            pLine.setQuantityProduct(BigDecimal.ZERO);
                            pLine.setTotalAmount(BigDecimal.ZERO);
                        }
                        production.addLivestockFeedProductionLineListItem(pLine);
                    }
                }
            }
        }
        response.setValues(production);
    }

    @Transactional
    public void livestockFeedProductionValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedProduction production = request.getContext().asType(LivestockFeedProduction.class);

        if (production != null ) {
            production = livestockFeedProductionService.Validate(production);
            if (production.getDispatchStock() != null) {
                response.setValue("dispatchStock", production.getDispatchStock());
            }
            response.setValue("status", production.getStatus());
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_PRODUCTION_NOT_FOUND));
        }

        response.setValues(production);
        response.setReload(true);
    }

    @Transactional
    public void livestockFeedProductionCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedProduction production = request.getContext().asType(LivestockFeedProduction.class);

        if (production != null ) {
            production = livestockFeedProductionService.Cancel(production);
            if (production.getDispatchStock() != null) {
                response.setValue("dispatchStock", production.getDispatchStock());
            }
            response.setValue("status", production.getStatus());
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_PRODUCTION_NOT_FOUND));
        }

        response.setValues(production);
        response.setReload(true);
    }
}
