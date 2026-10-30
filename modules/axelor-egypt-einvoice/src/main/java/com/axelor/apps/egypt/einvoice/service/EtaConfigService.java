package com.axelor.apps.egypt.einvoice.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.egypt.einvoice.db.EtaConfig;
import com.axelor.db.Query;
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
public class EtaConfigService {

  private static final Logger log = LoggerFactory.getLogger(EtaConfigService.class);

  public EtaConfig getActiveConfig(Company company) {
    if (company == null) {
      return null;
    }
    return Query.of(EtaConfig.class)
        .filter("self.company = :company AND self.isActive = true")
        .bind("company", company)
        .fetchOne();
  }

  public String getEtaAccessToken(EtaConfig config) throws Exception {
    if (config == null) {
      throw new IllegalArgumentException("ETA configuration is missing");
    }

    String clientId = config.getClientId();
    String clientSecret = config.getClientSecret();
    
    // In ETA Egypt, the Identity Server (OAuth Token) is hosted on id.eta.gov.eg / id.preprod.eta.gov.eg
    // which is distinct from the API gateway (api.invoicing.eta.gov.eg).
    String tokenUrl;
    String env = config.getEtaEnvironment();
    if ("production".equalsIgnoreCase(env) || (config.getEtaBaseUrl() != null && config.getEtaBaseUrl().contains("api.invoicing.eta.gov.eg"))) {
      tokenUrl = "https://id.eta.gov.eg/connect/token";
    } else if ("preprod".equalsIgnoreCase(env) || "sandbox".equalsIgnoreCase(env) || (config.getEtaBaseUrl() != null && config.getEtaBaseUrl().contains("preprod"))) {
      tokenUrl = "https://id.preprod.eta.gov.eg/connect/token";
    } else {
      tokenUrl = config.getEtaBaseUrl() + "/connect/token";
    }

    log.info("Requesting access token from ETA API: {}", tokenUrl);

    String requestBody = "grant_type=client_credentials"
        + "&client_id=" + clientId
        + "&client_secret=" + clientSecret;

    HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(tokenUrl))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      log.error("Failed to authenticate with ETA. Status: {}, Body: {}", response.statusCode(), response.body());
      throw new RuntimeException("ETA Authentication failed: " + response.body());
    }

    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
    return json.get("access_token").getAsString();
  }
}
