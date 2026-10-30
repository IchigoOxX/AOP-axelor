package com.axelor.apps.egypt.einvoice.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.egypt.einvoice.db.EtaConfig;
import com.axelor.apps.egypt.einvoice.db.EtaSubmissionLog;
import com.axelor.apps.egypt.einvoice.db.repo.EtaSubmissionLogRepository;
import com.axelor.auth.AuthUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EtaSubmissionService {

  private static final Logger log = LoggerFactory.getLogger(EtaSubmissionService.class);

  @Inject
  private EtaConfigService configService;

  @Inject
  private EtaInvoiceMapperService mapperService;

  @Inject
  private EtaSignerClientService signerService;

  @Inject
  private InvoiceRepository invoiceRepo;

  @Inject
  private EtaSubmissionLogRepository logRepo;

  @Transactional(rollbackOn = Exception.class)
  public JsonObject submitInvoice(Invoice invoice) throws Exception {
    if (invoice == null) {
      throw new IllegalArgumentException("Invoice cannot be null");
    }

    EtaConfig config = configService.getActiveConfig(invoice.getCompany());
    if (config == null) {
      throw new IllegalStateException("No active ETA configuration found for this company");
    }

    // 1. Generate JSON payload
    log.info("Mapping Invoice N° {} to ETA format", invoice.getInvoiceId());
    JsonObject unsignedPayload = mapperService.mapInvoiceToEtaJson(invoice);

    // 2. Sign document
    log.info("Sending Invoice JSON to signer service");
    JsonObject signedPayload = signerService.signDocument(unsignedPayload, config);

    // 3. Get ETA Access Token
    log.info("Requesting access token from ETA API");
    String accessToken = configService.getEtaAccessToken(config);

    // 4. Submit to ETA Portal
    String submissionUrl = config.getEtaBaseUrl() + "/api/v1.0/documentsubmissions";
    log.info("Submitting signed document to ETA API: {}", submissionUrl);

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(submissionUrl))
        .header("Authorization", "Bearer " + accessToken)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(signedPayload.toString()))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    log.info("ETA Portal response code: {}", response.statusCode());
    
    // Create Submission Log
    EtaSubmissionLog subLog = new EtaSubmissionLog();
    subLog.setInvoice(invoice);
    subLog.setSubmissionDate(LocalDateTime.now());
    subLog.setSubmittedBy(AuthUtils.getUser());
    subLog.setRequestPayload(signedPayload.toString());
    subLog.setResponsePayload(response.body());

    if (response.statusCode() != 202 && response.statusCode() != 200) {
      subLog.setStatusSelect("rejected");
      subLog.setErrorMessage("ETA returned status " + response.statusCode() + ": " + response.body());
      logRepo.save(subLog);
      
      invoice.setEtaStatusSelect(7); // Rejected / Invalid
      invoiceRepo.save(invoice);
      
      throw new RuntimeException("ETA submission rejected: " + response.body());
    }

    JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
    
    // Parse accepted / rejected documents
    String submissionId = responseJson.has("submissionId") ? responseJson.get("submissionId").getAsString() : "";
    JsonArray acceptedDocs = responseJson.has("acceptedDocuments") ? responseJson.getAsJsonArray("acceptedDocuments") : new JsonArray();
    JsonArray rejectedDocs = responseJson.has("rejectedDocuments") ? responseJson.getAsJsonArray("rejectedDocuments") : new JsonArray();

    subLog.setEtaLongId(submissionId);

    if (acceptedDocs.size() > 0) {
      JsonObject accepted = acceptedDocs.get(0).getAsJsonObject();
      String uuid = accepted.get("uuid").getAsString();
      String longId = accepted.get("longId").getAsString();

      subLog.setEtaUuid(uuid);
      subLog.setEtaLongId(longId);
      subLog.setStatusSelect("pending"); // Awaiting validation from ETA
      logRepo.save(subLog);

      // Update Invoice entity
      invoice.setEtaUuid(uuid);
      invoice.setEtaLongId(longId);
      invoice.setEtaStatusSelect(4); // Submitted
      invoice.setEtaSubmissionDate(LocalDateTime.now());
      invoice.setEtaSubmittedBy(AuthUtils.getUser());
      invoiceRepo.save(invoice);
      
      log.info("Invoice successfully submitted. UUID: {}, LongID: {}", uuid, longId);
    } else if (rejectedDocs.size() > 0) {
      JsonObject rejected = rejectedDocs.get(0).getAsJsonObject();
      String error = rejected.get("error").toString();

      subLog.setStatusSelect("rejected");
      subLog.setErrorMessage(error);
      logRepo.save(subLog);

      invoice.setEtaStatusSelect(7); // Rejected / Invalid
      invoiceRepo.save(invoice);

      throw new RuntimeException("ETA Submission rejected document: " + error);
    } else {
      subLog.setStatusSelect("rejected");
      subLog.setErrorMessage("No accepted or rejected documents listed in response.");
      logRepo.save(subLog);

      invoice.setEtaStatusSelect(7);
      invoiceRepo.save(invoice);

      throw new RuntimeException("Unknown ETA response format: " + response.body());
    }

    return responseJson;
  }

  @Transactional(rollbackOn = Exception.class)
  public JsonObject checkInvoiceStatus(Invoice invoice) throws Exception {
    if (invoice == null || invoice.getEtaUuid() == null || invoice.getEtaUuid().isEmpty()) {
      throw new IllegalArgumentException("Invoice does not have an ETA UUID");
    }

    EtaConfig config = configService.getActiveConfig(invoice.getCompany());
    if (config == null) {
      throw new IllegalStateException("No active ETA configuration found for this company");
    }

    String accessToken = configService.getEtaAccessToken(config);
    String statusUrl = config.getEtaBaseUrl() + "/api/v1.0/documents/" + invoice.getEtaUuid() + "/raw";
    log.info("Requesting invoice status from ETA API: {}", statusUrl);

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(statusUrl))
        .header("Authorization", "Bearer " + accessToken)
        .GET()
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new RuntimeException("Failed to fetch status from ETA: " + response.body());
    }

    JsonObject responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
    if (responseJson.has("status")) {
      String status = responseJson.get("status").getAsString();
      log.info("ETA Invoice Status: {}", status);
      
      // Map status back to Axelor Selection
      // egypt.eta.invoice.status.select
      // 0: Draft, 4: Submitted, 5: Valid, 6: Invalid, 7: Rejected, 8: Cancelled
      int statusVal = 4; // default submitted
      if ("Valid".equalsIgnoreCase(status)) {
        statusVal = 5;
      } else if ("Invalid".equalsIgnoreCase(status)) {
        statusVal = 6;
      } else if ("Cancelled".equalsIgnoreCase(status)) {
        statusVal = 8;
      } else if ("Rejected".equalsIgnoreCase(status)) {
        statusVal = 7;
      }
      
      invoice.setEtaStatusSelect(statusVal);
      invoiceRepo.save(invoice);
    }

    return responseJson;
  }
}
