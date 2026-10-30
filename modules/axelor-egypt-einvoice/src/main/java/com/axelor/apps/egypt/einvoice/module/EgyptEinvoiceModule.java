package com.axelor.apps.egypt.einvoice.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.egypt.einvoice.service.EtaConfigService;
import com.axelor.apps.egypt.einvoice.service.EtaInvoiceMapperService;
import com.axelor.apps.egypt.einvoice.service.EtaSignerClientService;
import com.axelor.apps.egypt.einvoice.service.EtaSubmissionService;

public class EgyptEinvoiceModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(EtaConfigService.class).asEagerSingleton();
    bind(EtaInvoiceMapperService.class).asEagerSingleton();
    bind(EtaSignerClientService.class).asEagerSingleton();
    bind(EtaSubmissionService.class).asEagerSingleton();
  }
}
