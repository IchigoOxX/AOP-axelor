package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.FleshChicksReception;
import com.gl.apps.pf.db.FleshLoadingProgram;
import com.gl.apps.pf.db.HatcherEntry;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.FleshLoadingProgramRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FleshLoadingProgramServiceImpl implements  FleshLoadingProgramService{
@Inject private FleshLoadingProgramRepository fleshLoadingProgramRepository;
    @Transactional
    public FleshLoadingProgram ActivateBreedingShed(FleshLoadingProgram fleshLoadingProgram) throws AxelorException {

        FleshLoadingProgram fleshLoadingProgram1 = fleshLoadingProgramRepository.find(fleshLoadingProgram.getId());

        if (fleshLoadingProgram1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));
        }
        if (!fleshLoadingProgram1.getFleshLoadingProgramLineList().isEmpty()) {
            fleshLoadingProgram1.getFleshLoadingProgramLineList().forEach(
                line -> {
                    if (line != null) {
                        if(line.getBreedingShed() != null) {
                            line.getBreedingShed().setStatus(BreedingShedRepository.STATUS_ACTIVATED);
                            line.getBreedingShed().setActivationDate(LocalDateTime.now());
                        }
                    }
                });
        }
        return fleshLoadingProgramRepository.save(fleshLoadingProgram1);
    }
    @Transactional
    public FleshLoadingProgram validate(FleshLoadingProgram fleshLoadingProgram) throws AxelorException {
        FleshLoadingProgram fleshLoadingProgram1 = fleshLoadingProgramRepository.find(fleshLoadingProgram.getId());

        if (fleshLoadingProgram1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.FLESH_LOADING_PROGRAM_NOT_FOUND));
        }
        fleshLoadingProgram1.setIsValid(true);

        return fleshLoadingProgramRepository.save(fleshLoadingProgram1);
    }

}
