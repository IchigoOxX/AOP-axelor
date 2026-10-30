package com.axelor.apps.egypt.einvoice.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.egypt.einvoice.db.EtaConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EtaInvoiceMapperService {

  private static final Logger log = LoggerFactory.getLogger(EtaInvoiceMapperService.class);
  
  @Inject
  private EtaConfigService configService;

  private static final DateTimeFormatter ISO_FORMATTER = 
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public JsonObject mapInvoiceToEtaJson(Invoice invoice) throws Exception {
    if (invoice == null) {
      throw new IllegalArgumentException("Invoice cannot be null");
    }

    Company company = invoice.getCompany();
    EtaConfig config = configService.getActiveConfig(company);
    if (config == null) {
      throw new IllegalStateException("No active ETA configuration found for company: " + (company != null ? company.getName() : "null"));
    }

    JsonObject doc = new JsonObject();

    // 1. Issuer
    JsonObject issuer = new JsonObject();
    JsonObject issuerAddress = new JsonObject();
    
    Address compAddr = company.getAddress();
    issuerAddress.addProperty("branchID", config.getDefaultBranchId() != null ? config.getDefaultBranchId() : "0");
    issuerAddress.addProperty("country", compAddr != null && compAddr.getCountry() != null ? compAddr.getCountry().getAlpha2Code() : "EG");
    issuerAddress.addProperty("governate", compAddr != null && compAddr.getCity() != null ? compAddr.getCity().getName() : "Cairo");
    issuerAddress.addProperty("regionCity", compAddr != null && compAddr.getCity() != null ? compAddr.getCity().getName() : "Nasr City");
    issuerAddress.addProperty("street", compAddr != null && compAddr.getStreetName() != null ? compAddr.getStreetName() : "Main Street");
    issuerAddress.addProperty("buildingNumber", compAddr != null && compAddr.getBuildingNumber() != null ? compAddr.getBuildingNumber() : "1");
    issuerAddress.addProperty("postalCode", compAddr != null && compAddr.getZip() != null ? compAddr.getZip() : "11511");
    
    issuer.add("address", issuerAddress);
    issuer.addProperty("type", config.getIssuerType() != null ? config.getIssuerType() : "B");
    issuer.addProperty("id", config.getTaxRegistrationNumber());
    issuer.addProperty("name", company.getName());
    doc.add("issuer", issuer);

    // 2. Receiver
    JsonObject receiver = new JsonObject();
    JsonObject receiverAddress = new JsonObject();
    
    Partner partner = invoice.getPartner();
    Address partAddr = invoice.getAddress() != null ? invoice.getAddress() : (partner != null ? partner.getMainAddress() : null);
    
    receiverAddress.addProperty("country", partAddr != null && partAddr.getCountry() != null ? partAddr.getCountry().getAlpha2Code() : "EG");
    receiverAddress.addProperty("governate", partAddr != null && partAddr.getCity() != null ? partAddr.getCity().getName() : "Egypt");
    receiverAddress.addProperty("regionCity", partAddr != null && partAddr.getCity() != null ? partAddr.getCity().getName() : "Cairo");
    receiverAddress.addProperty("street", partAddr != null && partAddr.getStreetName() != null ? partAddr.getStreetName() : "Customer Street");
    receiverAddress.addProperty("buildingNumber", partAddr != null && partAddr.getBuildingNumber() != null ? partAddr.getBuildingNumber() : "1");
    if (partAddr != null && partAddr.getZip() != null) {
      receiverAddress.addProperty("postalCode", partAddr.getZip());
    }

    receiver.add("address", receiverAddress);
    
    // Receiver Type & ID logic
    String partnerTaxNbr = invoice.getPartnerTaxNbr() != null ? invoice.getPartnerTaxNbr() : (partner != null ? partner.getTaxNbr() : null);
    if (partnerTaxNbr != null && !partnerTaxNbr.trim().isEmpty()) {
      receiver.addProperty("type", "B"); // Business
      receiver.addProperty("id", partnerTaxNbr.replaceAll("\\s+", ""));
    } else {
      // Default to Person if no tax registration number is provided
      receiver.addProperty("type", "P"); // Person
      receiver.addProperty("id", "");
    }
    receiver.addProperty("name", partner != null ? partner.getName() : "Walk-in Customer");
    doc.add("receiver", receiver);

    // 3. Document Details
    String docType = "I";
    if (invoice.getOperationTypeSelect() != null) {
      int opType = invoice.getOperationTypeSelect();
      if (opType == 4) {
        docType = "C"; // Customer Credit Note -> ETA Credit Note
      } else if (opType == 2) {
        docType = "D"; // Supplier Debit Note -> ETA Debit Note
      }
    }
    doc.addProperty("documentType", docType);
    doc.addProperty("documentTypeVersion", "1.0");
    
    java.time.LocalDate localDate = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : java.time.LocalDate.now();
    LocalDateTime issueDateTime = localDate.atStartOfDay();
    doc.addProperty("dateTimeIssued", ISO_FORMATTER.format(issueDateTime));
    doc.addProperty("taxpayerActivityCode", config.getActivityCode());
    doc.addProperty("internalID", invoice.getInvoiceId());

    // 4. Invoice Lines
    JsonArray lines = new JsonArray();
    BigDecimal totalDiscount = BigDecimal.ZERO;
    BigDecimal totalSalesAmount = BigDecimal.ZERO;
    BigDecimal totalNetAmount = BigDecimal.ZERO;
    BigDecimal totalTaxAmount = BigDecimal.ZERO;
    
    if (invoice.getInvoiceLineList() != null) {
      for (InvoiceLine line : invoice.getInvoiceLineList()) {
        if (line.getProduct() == null) continue;
        
        JsonObject lineObj = new JsonObject();
        lineObj.addProperty("description", line.getProductName() != null ? line.getProductName() : line.getProduct().getName());
        
        // EGS or GPC code mapping
        String prodCode = line.getProductCode() != null ? line.getProductCode() : line.getProduct().getCode();
        if (prodCode != null && prodCode.startsWith("EG-")) {
          lineObj.addProperty("itemType", "EGS");
        } else {
          lineObj.addProperty("itemType", "GPC");
        }
        lineObj.addProperty("itemCode", prodCode != null ? prodCode : "10001774");
        
        // Unit type mapping to official ETA codes
        String rawUnit = line.getUnit() != null ? line.getUnit().getName() : "EA";
        String unitCode = mapToEtaUnitCode(rawUnit);
        lineObj.addProperty("unitType", unitCode);
        
        BigDecimal qty = line.getQty() != null ? line.getQty() : BigDecimal.ONE;
        lineObj.addProperty("quantity", qty.doubleValue());
        lineObj.addProperty("internalCode", prodCode);
        
        BigDecimal price = line.getPrice() != null ? line.getPrice() : BigDecimal.ZERO;
        BigDecimal salesTotal = price.multiply(qty).setScale(5, RoundingMode.HALF_UP);
        lineObj.addProperty("salesTotal", salesTotal.doubleValue());
        
        // Discount
        BigDecimal lineDiscountAmount = line.getDiscountAmount() != null ? line.getDiscountAmount() : BigDecimal.ZERO;
        totalDiscount = totalDiscount.add(lineDiscountAmount);
        
        JsonObject discountObj = new JsonObject();
        discountObj.addProperty("rate", 0); // Can be 0 if amount is absolute
        discountObj.addProperty("amount", lineDiscountAmount.doubleValue());
        lineObj.add("discount", discountObj);
        
        BigDecimal netTotal = salesTotal.subtract(lineDiscountAmount).setScale(5, RoundingMode.HALF_UP);
        lineObj.addProperty("netTotal", netTotal.doubleValue());
        lineObj.addProperty("valueDifference", 0.0);
        
        JsonObject unitValue = new JsonObject();
        unitValue.addProperty("currencySold", invoice.getCurrency() != null ? invoice.getCurrency().getCode() : "EGP");
        unitValue.addProperty("amountEGP", price.doubleValue());
        unitValue.addProperty("amountSold", price.doubleValue());
        unitValue.addProperty("currencyExchangeRate", 1.0);
        lineObj.add("unitValue", unitValue);
        
        // Taxable items on line
        JsonArray taxableItems = new JsonArray();
        BigDecimal lineTaxTotal = BigDecimal.ZERO;
        
        if (line.getTaxLineSet() != null && !line.getTaxLineSet().isEmpty()) {
          for (TaxLine taxLine : line.getTaxLineSet()) {
            BigDecimal rate = taxLine.getValue() != null ? taxLine.getValue() : BigDecimal.ZERO;
            if (rate.compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal taxAmount = netTotal.multiply(rate.divide(BigDecimal.valueOf(100), 5, RoundingMode.HALF_UP)).setScale(5, RoundingMode.HALF_UP);
              lineTaxTotal = lineTaxTotal.add(taxAmount);
              
              JsonObject taxItem = new JsonObject();
              taxItem.addProperty("taxType", "T1"); // T1 is VAT
              // ETA standard VAT subType: V001 for general 14%, V003 for 5%, V009 for exempt
              String subType = "V001";
              if (rate.compareTo(BigDecimal.valueOf(5)) == 0) {
                subType = "V003";
              } else if (rate.compareTo(BigDecimal.ZERO) == 0) {
                subType = "V009";
              }
              taxItem.addProperty("subType", subType);
              taxItem.addProperty("rate", rate.doubleValue());
              taxItem.addProperty("amount", taxAmount.doubleValue());
              taxableItems.add(taxItem);
            }
          }
        }
        
        lineObj.add("taxableItems", taxableItems);
        
        BigDecimal lineTotal = netTotal.add(lineTaxTotal).setScale(5, RoundingMode.HALF_UP);
        lineObj.addProperty("total", lineTotal.doubleValue());
        
        lines.add(lineObj);
        
        totalSalesAmount = totalSalesAmount.add(salesTotal);
        totalNetAmount = totalNetAmount.add(netTotal);
        totalTaxAmount = totalTaxAmount.add(lineTaxTotal);
      }
    }
    
    doc.add("invoiceLines", lines);
    
    // 5. Totals
    doc.addProperty("totalSalesAmount", totalSalesAmount.setScale(5, RoundingMode.HALF_UP).doubleValue());
    doc.addProperty("totalDiscountAmount", totalDiscount.setScale(5, RoundingMode.HALF_UP).doubleValue());
    doc.addProperty("netAmount", totalNetAmount.setScale(5, RoundingMode.HALF_UP).doubleValue());
    
    // Tax Totals
    JsonArray taxTotals = new JsonArray();
    if (totalTaxAmount.compareTo(BigDecimal.ZERO) > 0) {
      JsonObject taxTotalObj = new JsonObject();
      taxTotalObj.addProperty("taxType", "T1");
      taxTotalObj.addProperty("amount", totalTaxAmount.setScale(5, RoundingMode.HALF_UP).doubleValue());
      taxTotals.add(taxTotalObj);
    }
    doc.add("taxTotals", taxTotals);
    
    BigDecimal totalAmount = totalNetAmount.add(totalTaxAmount).setScale(5, RoundingMode.HALF_UP);
    doc.addProperty("totalAmount", totalAmount.doubleValue());
    
    doc.addProperty("extraDiscountAmount", 0.0);
    doc.addProperty("totalItemsDiscountAmount", totalDiscount.doubleValue());

    // Wrap inside the standard documents array
    JsonObject root = new JsonObject();
    JsonArray documents = new JsonArray();
    documents.add(doc);
    root.add("documents", documents);

    return root;
  }

  private String mapToEtaUnitCode(String rawUnit) {
    if (rawUnit == null || rawUnit.trim().isEmpty()) {
      return "EA";
    }
    String u = rawUnit.trim().toLowerCase();
    switch (u) {
      case "kg":
      case "kilogram":
      case "kilo":
      case "كجم":
      case "كيلو":
      case "كيلوجرام":
        return "KGM";
      case "g":
      case "gram":
      case "جرام":
        return "GRM";
      case "ton":
      case "tonne":
      case "طن":
        return "TNE";
      case "l":
      case "ltr":
      case "liter":
      case "litre":
      case "لتر":
        return "LTR";
      case "box":
      case "صندوق":
      case "كرتونة":
        return "BX";
      case "crt":
      case "carton":
        return "CT";
      case "hour":
      case "hr":
      case "ساعة":
        return "HUR";
      case "day":
      case "يوم":
        return "DAY";
      case "m":
      case "meter":
      case "متر":
        return "MTR";
      case "ea":
      case "u":
      case "unit":
      case "piece":
      case "pce":
      case "طائر":
      case "كتكوت":
      case "بيضة":
      case "عدد":
      case "حبة":
      default:
        return "EA";
    }
  }
}
