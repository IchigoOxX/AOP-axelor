package com.axelor.apps.egypt.einvoice.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.egypt.einvoice.db.EtaConfig;
import com.axelor.apps.egypt.einvoice.service.EtaConfigService;
import com.axelor.apps.egypt.einvoice.service.EtaSubmissionService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.gson.JsonObject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EtaInvoiceController {

  private static final Logger log = LoggerFactory.getLogger(EtaInvoiceController.class);

  public void submitToEta(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      if (invoice == null || invoice.getId() == null) {
        response.setError("Please save the invoice first.");
        return;
      }
      
      // Reload invoice to ensure all relations are fully loaded
      invoice = Beans.get(com.axelor.apps.account.db.repo.InvoiceRepository.class).find(invoice.getId());

      log.info("Starting ETA submission for Invoice ID: {}", invoice.getId());
      JsonObject result = Beans.get(EtaSubmissionService.class).submitInvoice(invoice);
      
      response.setNotify("Invoice N° " + invoice.getInvoiceId() + " has been successfully submitted to ETA.");
      response.setReload(true);
    } catch (Exception e) {
      log.error("Error submitting invoice to ETA", e);
      TraceBackService.trace(response, e);
    }
  }

  public void checkEtaStatus(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      if (invoice == null || invoice.getId() == null) {
        response.setError("Invalid invoice selection.");
        return;
      }

      invoice = Beans.get(com.axelor.apps.account.db.repo.InvoiceRepository.class).find(invoice.getId());
      
      if (invoice.getEtaUuid() == null || invoice.getEtaUuid().trim().isEmpty()) {
        response.setError("This invoice has not been submitted to ETA yet.");
        return;
      }

      log.info("Checking ETA status for Invoice ID: {}, UUID: {}", invoice.getId(), invoice.getEtaUuid());
      JsonObject result = Beans.get(EtaSubmissionService.class).checkInvoiceStatus(invoice);
      
      String status = result.has("status") ? result.get("status").getAsString() : "Unknown";
      response.setNotify("ETA Invoice Status updated: " + status);
      response.setReload(true);
    } catch (Exception e) {
      log.error("Error checking ETA status", e);
      TraceBackService.trace(response, e);
    }
  }

  public void testConfigConnection(ActionRequest request, ActionResponse response) {
    try {
      EtaConfig config = request.getContext().asType(EtaConfig.class);
      if (config == null) {
        response.setError("Invalid configuration.");
        return;
      }

      log.info("Testing connection for ETA Config: {}", config.getTaxRegistrationNumber());
      
      // 1. Test local signer
      String token = Beans.get(EtaConfigService.class).getEtaAccessToken(config);
      
      response.setNotify("Connection test successful! Valid OAuth token received from ETA.");
    } catch (Exception e) {
      log.error("Error testing connection to ETA", e);
      TraceBackService.trace(response, e);
    }
  }
}
