package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.rpc.ActionRequest;
import com.gl.apps.pf.db.Breed;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.StartBreedingGuide;
import com.gl.apps.pf.db.repo.BreedRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

import java.util.List;

public class BreedRepo extends BreedRepository {

  @Inject private SequenceService sequenceService;


  @Override
  public Breed save(Breed entity) {
    if (entity.getBreedRef() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_BREEDSEQ, IncubatorsLoadingProgram.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREED_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }

      entity.setBreedRef(seq);
    }
    List<StartBreedingGuide> breedingStartList = entity.getStartBreedingGuideList();
    entity.setStartBreedingGuideList(breedingStartList);

    return super.save(entity);
  }

  @Override
  public void remove(Breed entity) {
    super.remove(entity);
  }
}

