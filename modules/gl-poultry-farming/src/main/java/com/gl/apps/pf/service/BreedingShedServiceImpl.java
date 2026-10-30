package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.Center;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.CenterRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BreedingShedServiceImpl implements BreedingShedService {
    @Inject
    private BreedingShedRepository breedingShedRepo;
    @Inject
    private StockLocationRepository stockLocationRepo;
    @Inject
    private CenterRepository centerRepo;

    ActionRequest request;

    @Override
    @Transactional
    public StockLocation createOrUpdateStockLocation(BreedingShed entity) throws AxelorException {
        if (entity == null) {
            return null;
        }
        StockLocation loc = entity.getStockLocation();
        if (loc == null) {
            loc = new StockLocation();
            String name = entity.getName() != null ? entity.getName() : entity.getReference();
            loc.setName(name != null ? name : "Breeding Shed");
            if (entity.getCenter() != null) {
                Center center = entity.getCenter();
                if (center.getPhysicalLocation() == null && center.getId() != null) {
                    center = centerRepo.find(center.getId());
                }
                if (center != null && center.getPhysicalLocation() != null) {
                    loc.setParentStockLocation(center.getPhysicalLocation());
                    loc.setCompany(center.getPhysicalLocation().getCompany());
                }
            }
            if (loc.getCompany() == null && entity.getUnit() != null && entity.getUnit().getCompany() != null) {
                loc.setCompany(entity.getUnit().getCompany());
            }
            if (loc.getCompany() == null && AuthUtils.getUser() != null) {
                loc.setCompany(AuthUtils.getUser().getActiveCompany());
            }
            loc.setTypeSelect(StockLocationRepository.TYPE_INTERNAL);
        }
        if (entity.getUnit() != null) {
            loc.setUnit(entity.getUnit());
        }
        loc = stockLocationRepo.save(loc);
        entity.setStockLocation(loc);
        return loc;
    }

    @Override
    @Transactional
    public BreedingShed onSave(BreedingShed entity) throws AxelorException {
        if (entity == null) {
            return null;
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
        if (entity.getStockLocation() == null && (entity.getName() != null || entity.getReference() != null)) {
            createOrUpdateStockLocation(entity);
        } else if (entity.getStockLocation() != null && entity.getUnit() != null && entity.getStockLocation().getUnit() == null) {
            StockLocation loc = entity.getStockLocation();
            loc.setUnit(entity.getUnit());
            stockLocationRepo.save(loc);
        }
        return entity;
    }

    @Transactional
    public BreedingShed ActivateBreedingShed(BreedingShed entity, LocalDateTime activationDate) throws AxelorException {
        if (entity == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
        }

        if (entity.getStatus() != BreedingShedRepository.STATUS_CLOSED && entity.getStatus() != BreedingShedRepository.STATUS_STOPPED ) {
            throw new AxelorException(
                    entity,
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_STATUS_NOT_ALLOWED),
                    entity.getReference());
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
            createOrUpdateStockLocation(entity);
        } else if (entity.getUnit() != null && entity.getStockLocation().getUnit() == null) {
            StockLocation loc = entity.getStockLocation();
            loc.setUnit(entity.getUnit());
            stockLocationRepo.save(loc);
        }

        entity.setActivationDate(activationDate);
        entity.setStatus(BreedingShedRepository.STATUS_ACTIVATED);
        return breedingShedRepo.save(entity);
    }

    @Transactional
    public BreedingShed StopBreedingShed(BreedingShed entity, LocalDateTime stoppedDate) throws AxelorException {
        if (entity == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
        }
        entity.setBreed(null);
        entity.setCurrentBatchNumber(null);
        entity.setFillingDate(null);
        entity.setCapacity(0);
        entity.setMalesSiloCapacity(0);
        entity.setFemalesNumber(0);
        entity.setMalesNumber(0);
        entity.setFemalesSiloCapacity(0);
        entity.setReceivedFemalesNumber(0);
        entity.setReceivedMalesNumber(0);
        entity.setStatus(BreedingShedRepository.STATUS_STOPPED);
        entity.setStoppedDate(stoppedDate);
        return breedingShedRepo.save(entity);
    }

    @Transactional
    public BreedingShed CloseBreedingShed(BreedingShed entity, LocalDateTime closedDate) throws AxelorException {
        if (entity == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                    I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
        }
        entity.setStatus(BreedingShedRepository.STATUS_CLOSED);
        entity.setClosedDate(closedDate);
        return breedingShedRepo.save(entity);
    }
}
