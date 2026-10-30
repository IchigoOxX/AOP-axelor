package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.LivestockFeedExternalExit;
import com.gl.apps.pf.db.LivestockFeedExternalExitLine;
import com.gl.apps.pf.db.LivestockFeedProduction;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.LivestockFeedExternalExitService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class LivestockFeedExternalExitController {
    @Inject private LivestockFeedExternalExitService livestockFeedExternalExitService;

    public void getLinesFromDeliveryCustomer(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedExternalExit externalExit = request.getContext().asType(LivestockFeedExternalExit.class);
        if (externalExit != null && externalExit.getCustomerDelivery() != null) {
            externalExit.clearLivestockFeedExternalExitLineList();

            int nbr = 1;
            for (StockMoveLine x : externalExit.getCustomerDelivery().getStockMoveLineList()) {
                LivestockFeedExternalExitLine rline = new LivestockFeedExternalExitLine();
                rline.setProduct(x.getProduct());
                rline.setCodeProduct(x.getProduct().getCode());
                rline.setUmProduct(x.getProduct().getUnit().getName());
                rline.setCapacity(x.getProduct().getCapacity());
                rline.setExitQuantity(x.getRealQty());

                BigDecimal capacity = BigDecimal.valueOf(x.getProduct().getCapacity());
                if (capacity.compareTo(BigDecimal.ZERO) != 0) {
                    rline.setUnitNumber((x.getRealQty().divide(BigDecimal.valueOf(x.getProduct().getCapacity())).intValue()));
                }
                else  {
                    rline.setUnitNumber(0);
                }

                rline.setSalePrice(x.getProduct().getSalePrice());
                rline.setSaleTotalAmount(x.getProduct().getSalePrice().multiply(x.getRealQty()));

                externalExit.addLivestockFeedExternalExitLineListItem(rline);
                nbr++;
            }
        }
        response.setValues(externalExit);
    }

    @Transactional
    public void livestockFeedExternalExitValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedExternalExit externalExit = request.getContext().asType(LivestockFeedExternalExit.class);

        if (externalExit != null ) {
            livestockFeedExternalExitService.Validate(externalExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_NOT_FOUND));
        }

        response.setValues(externalExit);
        response.setReload(true);
    }

    @Transactional
    public void livestockFeedExternalExitCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LivestockFeedExternalExit externalExit = request.getContext().asType(LivestockFeedExternalExit.class);

        if (externalExit != null ) {
            livestockFeedExternalExitService.Cancel(externalExit);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_NOT_FOUND));
        }

        response.setValues(externalExit);
        response.setReload(true);
    }

}

