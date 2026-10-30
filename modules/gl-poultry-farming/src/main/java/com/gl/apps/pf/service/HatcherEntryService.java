package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.IncubatorEntry;

public interface HatcherEntryService {
    public HatcherEntry Validate(HatcherEntry hatcherEntry) throws AxelorException;
    public HatcherEntry Cancel(HatcherEntry hatcherEntry) throws AxelorException;
    public HatcherEntry Revalidate(HatcherEntry hatcherEntry) throws AxelorException;
}
