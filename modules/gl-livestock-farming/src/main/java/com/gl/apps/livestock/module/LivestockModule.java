package com.gl.apps.livestock.module;

import com.axelor.app.AxelorModule;
import com.gl.apps.livestock.service.LivestockOperationsService;
import com.gl.apps.livestock.service.LivestockOperationsServiceImpl;

public class LivestockModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(LivestockOperationsService.class).to(LivestockOperationsServiceImpl.class);
  }
}
