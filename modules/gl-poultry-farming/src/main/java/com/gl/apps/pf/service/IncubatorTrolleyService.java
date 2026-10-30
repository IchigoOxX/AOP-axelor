package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.IncubatorTrolley;

public interface IncubatorTrolleyService {
    public IncubatorTrolley StopIncubatorTrolley(IncubatorTrolley entity, IncubatorTrolley incubatorTrolley) throws AxelorException;
}
