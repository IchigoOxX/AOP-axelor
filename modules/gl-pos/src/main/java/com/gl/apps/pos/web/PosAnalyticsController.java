package com.gl.apps.pos.web;

import com.gl.apps.pos.service.AnalyticsService;
import com.gl.apps.pos.service.ReceiptService;
import com.gl.apps.pos.service.BarcodeService;
import com.gl.apps.pos.service.LoyaltyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.gl.apps.pos.db.LoyaltyCustomer;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

public class PosAnalyticsController {

  private static final Logger LOG = LoggerFactory.getLogger(PosAnalyticsController.class);

  @Inject private AnalyticsService analyticsService;
  @Inject private ReceiptService receiptService;
  @Inject private BarcodeService barcodeService;
  @Inject private LoyaltyService loyaltyService;

  /**
   * Generate daily sales report
   */
  public void generateDailySalesReport(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> data = request.getContext();
      Long terminalId = ((Number) data.get("terminalId")).longValue();
      String date = (String) data.get("date");

      Map<String, Object> report = analyticsService.generateDailySalesReport(terminalId, date);
      response.setValue("report", report);
    } catch (Exception e) {
      LOG.error("Error generating daily report", e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Get payment method breakdown
   */
  public void getPaymentMethodBreakdown(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> data = request.getContext();
      Long terminalId = ((Number) data.get("terminalId")).longValue();
      String startDate = (String) data.get("startDate");
      String endDate = (String) data.get("endDate");

      Map<String, Object> breakdown = analyticsService.getPaymentMethodBreakdown(terminalId, startDate, endDate);
      response.setValue("breakdown", breakdown);
    } catch (Exception e) {
      LOG.error("Error getting payment breakdown", e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Get top selling items
   */
  public void getTopSellingItems(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> data = request.getContext();
      Long terminalId = ((Number) data.get("terminalId")).longValue();
      int limit = ((Number) data.getOrDefault("limit", 10)).intValue();

      List<Map<String, Object>> items = analyticsService.getTopSellingItems(terminalId, limit, "all");
      response.setValue("items", items);
    } catch (Exception e) {
      LOG.error("Error getting top items", e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Generate receipt
   */
  public void generateReceipt(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder order = request.getContext().asType(SaleOrder.class);
      String format = (String) request.getContext().get("format");

      String receipt = receiptService.formatReceipt(order, format);
      response.setValue("receipt", receipt);
    } catch (Exception e) {
      LOG.error("Error generating receipt", e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Scan barcode
   */
  public void scanBarcode(ActionRequest request, ActionResponse response) {
    try {
      String barcode = (String) request.getContext().get("barcode");
      var product = barcodeService.scanBarcode(barcode);
      response.setValue("product", product);
    } catch (Exception e) {
      LOG.error("Error scanning barcode", e);
      response.setError(e.getMessage());
    }
  }

  /**
   * Get loyalty customer info
   */
  public void getLoyaltyCustomerInfo(ActionRequest request, ActionResponse response) {
    try {
      String loyaltyNumber = (String) request.getContext().get("loyaltyNumber");
      LoyaltyCustomer customer = loyaltyService.getLoyaltyCustomerByNumber(loyaltyNumber);

      if (customer != null) {
        response.setValue("customer", customer);
        response.setValue("rewards", loyaltyService.getAvailableRewards(customer));
      } else {
        response.setError("Loyalty customer not found");
      }
    } catch (Exception e) {
      LOG.error("Error getting loyalty info", e);
      response.setError(e.getMessage());
    }
  }
}





