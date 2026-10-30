package com.gl.apps.pf.web;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.Breed;
import com.gl.apps.pf.db.BreedingShed;
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.IncubatorsLoadingProgram;
import com.gl.apps.pf.db.Center;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.CenterRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.BreedingShedService;
import com.gl.apps.pf.service.IncubatorService;
import com.gl.apps.pf.service.IncubatorsLoadingProgramService;
import com.google.inject.Inject;

import jakarta.persistence.EntityManager;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class BreedingShedController {
    @Inject
    private BreedingShedRepository breedingShedRepo;
    @Inject private BreedingShedService breedingShedService;
    @Inject private CenterRepository centerRepo;

    public void onNew(ActionRequest request, ActionResponse response) {
        try {
            BreedingShed shed = request.getContext().asType(BreedingShed.class);
            if (shed != null && shed.getCenter() != null) {
                Center center = shed.getCenter();
                if (center.getUnit() == null && center.getId() != null) {
                    center = centerRepo.find(center.getId());
                }
                if (center != null && center.getUnit() != null) {
                    response.setValue("unit", center.getUnit());
                }
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    public void onCenterChange(ActionRequest request, ActionResponse response) {
        try {
            BreedingShed shed = request.getContext().asType(BreedingShed.class);
            if (shed != null && shed.getCenter() != null) {
                Center center = shed.getCenter();
                if (center.getUnit() == null && center.getId() != null) {
                    center = centerRepo.find(center.getId());
                }
                if (center != null && center.getUnit() != null) {
                    response.setValue("unit", center.getUnit());
                }
                if (center != null && center.getType() != null && shed.getType() == null) {
                    response.setValue("type", center.getType());
                }
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    @Transactional
    public void onSave(ActionRequest request, ActionResponse response) {
        try {
            BreedingShed shed = request.getContext().asType(BreedingShed.class);
            if (shed != null) {
                shed = breedingShedService.onSave(shed);
                if (shed.getUnit() != null) {
                    response.setValue("unit", shed.getUnit());
                }
                if (shed.getStockLocation() != null) {
                    response.setValue("stockLocation", shed.getStockLocation());
                }
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    @Transactional
    public void ActivateBrShed(ActionRequest request, ActionResponse response) {
        LocalDateTime activationDate = (LocalDateTime) request.getContext().get("activationDate");

        try {
            BreedingShed breedingshed =
                    Beans.get(BreedingShedRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            if (breedingshed != null ) {
                breedingshed = Beans.get(BreedingShedService.class).ActivateBreedingShed(breedingshed, activationDate);
                if (breedingshed.getStockLocation() != null) {
                    response.setValue("stockLocation", breedingshed.getStockLocation());
                }
                if (breedingshed.getUnit() != null) {
                    response.setValue("unit", breedingshed.getUnit());
                }
                response.setValues(breedingshed);
                response.setCanClose(true);
            }
            else{
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
        response.setReload(true);
    }

    @Transactional
    public void stop(ActionRequest request, ActionResponse response) {
        LocalDateTime stoppedDate = (LocalDateTime) request.getContext().get("stoppedDate");
        try {
            BreedingShed breedingShed = request.getContext().asType(BreedingShed.class);
            if (breedingShed.getId() != null) {
                breedingShed = breedingShedRepo.find(breedingShed.getId());
                breedingShedService.StopBreedingShed(breedingShed, stoppedDate);

            }
            else  {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
            }
            response.setReload(true);
            response.setValues(breedingShed);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    @Transactional
    public void StopBreedingShed(ActionRequest request, ActionResponse response) {
        LocalDateTime stoppedDate = (LocalDateTime) request.getContext().get("stoppedDate");
        try {
            BreedingShed breedingshed =
                    Beans.get(BreedingShedRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            LocalDateTime localDateTime = LocalDateTime.now();
            if (breedingshed != null ) {
                breedingshed = Beans.get(BreedingShedService.class).StopBreedingShed(breedingshed, stoppedDate);
                breedingshed.setStoppedDate(localDateTime);
                response.setReload(true);
                response.setCanClose(true);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

    @Transactional
    public void CloseBreedingShed(ActionRequest request, ActionResponse response) {
        LocalDateTime closedDate = (LocalDateTime) request.getContext().get("closedDate");
        try {
            BreedingShed breedingshed =
                    Beans.get(BreedingShedRepository.class)
                            .find(Long.parseLong(request.getContext().get("_id").toString()));
            if (breedingshed != null ) {
                breedingshed = Beans.get(BreedingShedService.class).CloseBreedingShed(breedingshed, closedDate);
                response.setReload(true);
                response.setCanClose(true);
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get(PoultryFarmingExceptionMessage.BREEDING_SHED_NOT_FOUND));
            }
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
    }

}

