package com.gl.apps.pos.service.impl;

import com.gl.apps.pos.service.ReceiptService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ReceiptServiceImpl implements ReceiptService {

  private static final Logger LOG = LoggerFactory.getLogger(ReceiptServiceImpl.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Override
  public String generateTextReceipt(SaleOrder order) {
    LOG.info("Generating text receipt for order: {}", order.getSaleOrderSeq());

    StringBuilder receipt = new StringBuilder();
    receipt.append("═════════════════════════════════════\\.");
    receipt.append("          SALES RECEIPT\\.");
    receipt.append("═════════════════════════════════════\\.");
    receipt.append("Receipt #: ").append(order.getSaleOrderSeq()).append("\\.");
    receipt.append("Date/Time: ").append(order.getCreatedOn().format(DATE_FORMATTER)).append("\\.");
    receipt.append("Terminal: ").append(order.getStore().getName()).append("\\.");
    receipt.append("─────────────────────────────────────\\.");
    receipt.append(String.format("%-20s %5s %10s %10s\\.", "ITEM", "QTY", "PRICE", "TOTAL"));
    receipt.append("─────────────────────────────────────\\.");

    for (SaleOrderLine line : order.getSaleOrderLineList()) {
      receipt.append(String.format("%-20s %5s %10s %10s\\.", 
        truncate(line.getProduct().getName(), 20), 
        line.getQty(), 
        line.getPrice(), 
        line.getInTaxTotal()));
    }

    receipt.append("─────────────────────────────────────\\.");
    receipt.append(String.format("Subtotal: %40s\\.", order.getExTaxTotal()));
    receipt.append(String.format("Tax: %40s\\.", order.getTaxTotal()));
    receipt.append("═════════════════════════════════════\\.");
    receipt.append(String.format("TOTAL: %40s\\.", order.getInTaxTotal()));
    receipt.append("═════════════════════════════════════\\.");
    receipt.append("Thank you, come again!\\.");

    return receipt.toString();
  }

  @Override
  public String generateHtmlReceipt(SaleOrder order) {
    LOG.info("Generating HTML receipt for order: {}", order.getSaleOrderSeq());

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html>\\.<html>\\.<head>\\.<title>Receipt</title>\\.");
    html.append("<style>body { font-family: Arial; margin: 20px; } table { width: 100%; }</style>\\.");
    html.append("</head>\\.<body>\\.<h1>SALES RECEIPT</h1>\\.");
    html.append("<p><strong>Receipt #:</strong> ").append(order.getSaleOrderSeq()).append("</p>\\.");
    html.append("<table><tr><th>Item</th><th>Qty</th><th>Price</th><th>Total</th></tr>\\.");

    for (SaleOrderLine line : order.getSaleOrderLineList()) {
      html.append("<tr><td>").append(line.getProduct().getName())
          .append("</td><td>").append(line.getQty())
          .append("</td><td>").append(line.getPrice())
          .append("</td><td>").append(line.getInTaxTotal()).append("</td></tr>\\.");
    }

    html.append("</table>\\.<p>Total: ").append(order.getInTaxTotal()).append("</p>\\.</body>\\.</html>\\.");
    return html.toString();
  }

  @Override
  public void printReceipt(SaleOrder order, String printerName) {
    LOG.info("Printing receipt for order: {} to printer: {}", order.getSaleOrderSeq(), printerName);
  }

  @Override
  public void emailReceipt(SaleOrder order, String email) {
    LOG.info("Emailing receipt for order: {} to {}", order.getSaleOrderSeq(), email);
    if (!isValidEmail(email)) throw new IllegalArgumentException("Invalid email");
  }

  @Override
  public void smsReceipt(SaleOrder order, String phoneNumber) {
    LOG.info("Sending SMS receipt for order: {} to {}", order.getSaleOrderSeq(), phoneNumber);
    if (!isValidPhoneNumber(phoneNumber)) throw new IllegalArgumentException("Invalid phone");
  }

  @Override
  public String getReceiptTemplate(String templateName) {
    LOG.debug("Getting receipt template: {}", templateName);
    return "";
  }

  @Override
  public String formatReceipt(SaleOrder order, String format) {
    return format.equalsIgnoreCase("html") ? generateHtmlReceipt(order) : generateTextReceipt(order);
  }

  @Override
  public String saveReceiptToFile(SaleOrder order, String format) {
    LOG.info("Saving receipt for order: {} to file as {}", order.getSaleOrderSeq(), format);
    return "receipt_" + order.getSaleOrderSeq() + "." + format;
  }

  @Override
  public String generateReceiptNumber(SaleOrder order) {
    return order.getSaleOrderSeq();
  }

  @Override
  public String addLoyaltyPointsToReceipt(String receipt, Long loyaltyPoints) {
    return receipt + "\\.\\.Loyalty Points Earned: " + loyaltyPoints + "\\.";
  }

  @Override
  public String addPromotionalMessage(String receipt) {
    return receipt + "\\.\\.Special Offer: Get 10% OFF on your next order!\\.";
  }

  private String truncate(String str, int length) {
    return str == null || str.length() <= length ? str : str.substring(0, length - 1) + "…";
  }

  private boolean isValidEmail(String email) {
    return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\..[A-Za-z]{2,}$", email);
  }

  private boolean isValidPhoneNumber(String phone) {
    return Pattern.matches("^[+]?[0-9]{10,13}$", phone.replaceAll("\\.s|-", ""));
  }
}


