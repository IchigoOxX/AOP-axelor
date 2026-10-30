package com.gl.apps.pos.service;

import com.gl.apps.pos.service.ReceiptService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public interface ReceiptService {
	public String generateTextReceipt(SaleOrder order);
	public String generateHtmlReceipt(SaleOrder order);
	public void printReceipt(SaleOrder order, String printerName);
	public void emailReceipt(SaleOrder order, String email);
	public void smsReceipt(SaleOrder order, String phoneNumber);
	public String getReceiptTemplate(String templateName);
	public String formatReceipt(SaleOrder order, String format);
	public String saveReceiptToFile(SaleOrder order, String format);
	public String generateReceiptNumber(SaleOrder order);
	public String addLoyaltyPointsToReceipt(String receipt, Long loyaltyPoints);
	public String addPromotionalMessage(String receipt);
}
