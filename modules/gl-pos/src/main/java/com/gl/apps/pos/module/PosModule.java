/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.gl.apps.pos.module;

import com.axelor.app.AxelorModule;
import com.gl.apps.pos.service.ShiftService;
import com.gl.apps.pos.service.ShiftServiceImpl;

import com.gl.apps.pos.service.BarcodeService;
import com.gl.apps.pos.service.impl.BarcodeServiceImpl;
import com.gl.apps.pos.service.LoyaltyService;
import com.gl.apps.pos.service.impl.LoyaltyServiceImpl;
import com.gl.apps.pos.service.AnalyticsService;
import com.gl.apps.pos.service.impl.AnalyticsServiceImpl;
import com.gl.apps.pos.service.ReceiptService;
import com.gl.apps.pos.service.impl.ReceiptServiceImpl;
import com.gl.apps.pos.service.PosSequenceService;
import com.gl.apps.pos.service.impl.PosSequenceServiceImpl;

public class PosModule extends AxelorModule {

  @Override
  protected void configure() {

        bind(ShiftService.class).to(ShiftServiceImpl.class);
    
    // Migrated from axelor-pos
    bind(BarcodeService.class).to(BarcodeServiceImpl.class);
    bind(LoyaltyService.class).to(LoyaltyServiceImpl.class);
    bind(AnalyticsService.class).to(AnalyticsServiceImpl.class);
    bind(ReceiptService.class).to(ReceiptServiceImpl.class);
    bind(PosSequenceService.class).to(PosSequenceServiceImpl.class);
  }
}

