package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;

public interface PartnerService {
    public Partner AddComptInfo(Partner partner) throws AxelorException;
}
