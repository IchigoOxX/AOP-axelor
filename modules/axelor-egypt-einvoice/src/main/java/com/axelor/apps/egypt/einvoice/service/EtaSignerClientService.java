package com.axelor.apps.egypt.einvoice.service;

import com.axelor.apps.egypt.einvoice.db.EtaConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Singleton;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EtaSignerClientService {

  private static final Logger log = LoggerFactory.getLogger(EtaSignerClientService.class);

  public JsonObject signDocument(JsonObject unsignedPayload, EtaConfig config) throws Exception {
    if (config == null) {
      throw new IllegalArgumentException("ETA configuration is missing");
    }

    String signerUrl = config.getSignerServiceUrl();
    log.info("Sending document to local signer service: {}", signerUrl);

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    String requestBody = unsignedPayload.toString();

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(signerUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody));

    // Basic Auth if username and password are provided in the config
    String authUser = config.getSignerAuthUser();
    String authPass = config.getSignerAuthPassword();
    if (authUser != null && !authUser.trim().isEmpty() && authPass != null && !authPass.trim().isEmpty()) {
      String authHeader = "Basic " + Base64.getEncoder().encodeToString((authUser + ":" + authPass).getBytes("UTF-8"));
      requestBuilder.header("Authorization", authHeader);
    }
    
    // Add Token PIN to headers if configured so signer can unlock token dynamically if needed
    String pin = config.getTokenPin();
    if (pin != null && !pin.trim().isEmpty()) {
      requestBuilder.header("Token-Pin", pin);
    }

    HttpRequest request = requestBuilder.build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      log.error("Signer service returned status: {}, body: {}", response.statusCode(), response.body());
      throw new RuntimeException("Digital signature failed: " + response.body());
    }

    log.info("Document signed successfully by signer service.");
    return JsonParser.parseString(response.body()).getAsJsonObject();
  }
}
