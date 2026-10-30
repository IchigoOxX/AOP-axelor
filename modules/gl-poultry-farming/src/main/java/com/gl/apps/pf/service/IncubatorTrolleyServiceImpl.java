package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleysChargingLineRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleysChargingRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IncubatorTrolleyServiceImpl implements IncubatorTrolleyService{
    @Inject private IncubatorTrolleyRepository incubatorTrolleyRepository;
    @Inject private IncubatorTrolleysChargingLineRepository incubatorTrolleysChargingLineRepository;
    @Inject private IncubatorTrolleysChargingRepository incubatorTrolleysChargingRepository;
    @Transactional
    public IncubatorTrolley StopIncubatorTrolley(IncubatorTrolley entity, IncubatorTrolley incubatorTrolley) throws AxelorException {

        if (entity == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.INCUBATOR_TROLLEY_NOT_FOUND));
        }
        IncubatorTrolleysCharging incubatorTrolleysCharging = new IncubatorTrolleysCharging();

        if (entity.getStatus().equals(IncubatorTrolleyRepository.STATUS_BUSY)) {
            IncubatorTrolleysChargingLine incubatorTrolleysChargingLine =
                    incubatorTrolleysChargingLineRepository.all()
                            .filter("self.incubatorTrolley = ?1 and self.incubatorTrolleysCharging.status = 1", entity)
                            .order("incubatorTrolleysCharging.chargingDate")
                            .fetchOne();

            if(incubatorTrolleysChargingLine == null ) {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("The incubator trolley: " + entity.getReference() + " was not charged"));
            }

            if (incubatorTrolleysChargingLine != null && incubatorTrolleysChargingLine.getChargingLineDetailList() != null) {
                incubatorTrolleysCharging.setChargingDate(LocalDate.now());
                incubatorTrolleysCharging.setHatchery(incubatorTrolleysChargingLine.getIncubatorTrolley().getHatchery());

                incubatorTrolleysChargingLine.setIncubatorTrolley(incubatorTrolley);
                incubatorTrolleysChargingLine.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_BUSY);
                incubatorTrolleysCharging.addIncubatorTrolleysChargingLineListItem(incubatorTrolleysChargingLine);


                List<IncubatorTrolleysChargingLineDetail> detailsToAdd = new ArrayList<>(incubatorTrolleysChargingLine.getChargingLineDetailList());

                for (IncubatorTrolleysChargingLineDetail incubatorTrolleysChargingLineDetail : detailsToAdd) {
                    incubatorTrolleysCharging.getIncubatorTrolleysChargingLineList().get(0).addChargingLineDetailListItem(incubatorTrolleysChargingLineDetail);
                }
                incubatorTrolleysCharging.setStatus(IncubatorTrolleysChargingRepository.STATUS_VALIDATED);

                incubatorTrolleysChargingRepository.save(incubatorTrolleysCharging);
                entity.setStatus(IncubatorTrolleyRepository.STATUS_STOPPED);
            }
        }
        return incubatorTrolleyRepository.save(entity);
    }
}
