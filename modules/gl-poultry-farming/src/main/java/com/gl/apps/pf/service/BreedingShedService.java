package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.gl.apps.pf.db.BreedingShed;

import java.time.LocalDateTime;

public interface BreedingShedService {
    public BreedingShed ActivateBreedingShed(BreedingShed entity, LocalDateTime activationDate) throws AxelorException;
    public BreedingShed StopBreedingShed(BreedingShed entity, LocalDateTime stoppedDate) throws AxelorException;
    public BreedingShed CloseBreedingShed(BreedingShed entity, LocalDateTime closedDate) throws AxelorException;
    public BreedingShed onSave(BreedingShed entity) throws AxelorException;
    public com.axelor.apps.stock.db.StockLocation createOrUpdateStockLocation(BreedingShed entity) throws AxelorException;
}
