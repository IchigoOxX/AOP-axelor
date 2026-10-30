package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
/* import com.axelor.apps.stock.service.stockmove.print.StockMovePrintService; */
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.LogisticsSheetRepository;
import com.gl.apps.pf.service.LogisticsSheetService;
import com.gl.apps.pf.service.ProductConsumptionService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogisticsSheetController {
    @Inject
    private LogisticsSheetService logisticsSheetService;
    @Inject
    private PurchaseOrderLineRepository purchaseOrderLineRepository;
    public void logisticsSheetValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LogisticsSheet logisticsSheet = request.getContext().asType(LogisticsSheet.class);

        if (logisticsSheet != null ) {
            logisticsSheetService.Validate(logisticsSheet);
        }

        response.setValues(logisticsSheet);
        response.setReload(true);
    }

    public void logisticsSheetRealize(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LogisticsSheet logisticsSheet = request.getContext().asType(LogisticsSheet.class);

        if (logisticsSheet != null ) {
            logisticsSheetService.Realize(logisticsSheet);
        }

        response.setValues(logisticsSheet);
        response.setReload(true);
    }

    public void getPurchaseOrderLineFromProFormat(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LogisticsSheet logisticsSheet = request.getContext().asType(LogisticsSheet.class);
        if (logisticsSheet != null) {
            logisticsSheet.clearPurchaseOrderLineList();
            int nbr = 1;
            for (PurchaseOrderLine x : purchaseOrderLineRepository.all().fetch()) {
                if (x.getPurchaseOrder() == logisticsSheet.getProFormat()) {
                    PurchaseOrderLine rline = new PurchaseOrderLine();
                    rline.setProduct(x.getProduct());
                    rline.setQty(x.getQty());
                    rline.setProductName(x.getProduct().getFullName());
                    logisticsSheet.addPurchaseOrderLineListItem(rline);
                }
            }
        }
        response.setValues(logisticsSheet);
    }

    @Transactional
    public void getAttachmentsFromProduct(ActionRequest request, ActionResponse response)
            throws AxelorException {
        LogisticsSheet logisticsSheet = request.getContext().asType(LogisticsSheet.class);
//        for (Attachment x : logisticsSheet.getAttachmentList()) {
//            if (x.getLogisticSheet() == null ){
//
//            }
//        }
        if (logisticsSheet != null && logisticsSheet.getPurchaseOrderLineList() != null) {
            for (PurchaseOrderLine line : logisticsSheet.getPurchaseOrderLineList()) {
                int nbr = 1;
                if (line != null && line.getProduct() != null &&
                        line.getProduct().getProductAttachmentTypeList() != null) {
                    for (ProductAttachmentType line1 : line.getProduct().getProductAttachmentTypeList()) {
                        Attachment attachment = new Attachment();
                        attachment.setLogisticSheet(logisticsSheet);
                        attachment.setProductAttachmentType(line1);
                        attachment.setLineNumber(nbr);
                        attachment.setAttachmentRequired(line1.getIsRequired());
                        attachment.setAttachmentTypeTitle(line1.getTitle());
                        logisticsSheet.addAttachmentListItem(attachment);
                        nbr++;
                    }
                }
            }
        }
        response.setValues(logisticsSheet);
    }



}

