package com.gl.apps.pf.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.Center;
import com.gl.apps.pf.db.Ipoultryfarming;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.CenterRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

public class BreedingShedRepo extends BreedingShedRepository {

  @Inject private SequenceService sequenceService;
  @Inject private StockLocationRepository stockLocationRepo;
  @Inject private CenterRepository centerRepo;

  @Override
  public BreedingShed save(BreedingShed entity) {
    if (entity.getReference() == null) {
      String seq = null;
      try {
        seq =
            sequenceService.getSequenceNumber(Ipoultryfarming.SEQ_BREEDINGSHEDSEQ, BreedingShed.class, "reference", null);
      } catch (AxelorException e) {
        TraceBackService.traceExceptionFromSaveMethod(e);
      }
      if (seq == null) {
        try {
          throw new AxelorException(
              entity,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(PoultryFarmingExceptionMessage.BREEDINGSHED_SEQUENCE_NOT_FOUND),
              I18n.get(BaseExceptionMessage.EXCEPTION));
        } catch (AxelorException e) {
          throw new RuntimeException(e);
        }
      }
      entity.setReference(seq);
    }

    if (entity.getUnit() == null && entity.getCenter() != null) {
      Center center = entity.getCenter();
      if (center.getUnit() == null && center.getId() != null) {
        center = centerRepo.find(center.getId());
      }
      if (center != null && center.getUnit() != null) {
        entity.setUnit(center.getUnit());
      }
    }

    if (entity.getStockLocation() == null) {
      StockLocation location = new StockLocation();
      location.setTypeSelect(StockLocationRepository.TYPE_INTERNAL);
      String name = entity.getName() != null ? entity.getName() : entity.getReference();
      location.setName(name != null ? name : "Breeding Shed");
      Company company = entity.getUnit() != null ? entity.getUnit().getCompany() : (AuthUtils.getUser() != null ? AuthUtils.getUser().getActiveCompany() : null);
      location.setCompany(company);
      location.setUnit(entity.getUnit());
      if (entity.getCenter() != null) {
        Center center = entity.getCenter();
        if (center.getPhysicalLocation() == null && center.getId() != null) {
          center = centerRepo.find(center.getId());
        }
        if (center != null && center.getPhysicalLocation() != null) {
          location.setParentStockLocation(center.getPhysicalLocation());
          if (location.getCompany() == null) {
            location.setCompany(center.getPhysicalLocation().getCompany());
          }
        }
      }
      location = stockLocationRepo.save(location);
      entity.setStockLocation(location);
    } else if (entity.getStockLocation().getUnit() == null && entity.getUnit() != null) {
      StockLocation location = entity.getStockLocation();
      location.setUnit(entity.getUnit());
      stockLocationRepo.save(location);
    }

    return super.save(entity);
  }

  @Override
  public void remove(BreedingShed entity) {
    super.remove(entity);
  }
}
