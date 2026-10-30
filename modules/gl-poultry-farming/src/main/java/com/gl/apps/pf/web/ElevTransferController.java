package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.BreedingShedRepository;
import com.gl.apps.pf.db.repo.ElevTransferRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ElevTransferService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ElevTransferController {
    @Inject
    private ElevTransferService elevTransferService;

    public void getElevTransLinesFromCenter(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ElevTransfer elevTransfer = request.getContext().asType(ElevTransfer.class);
        if (elevTransfer != null && elevTransfer.getFromCenter() != null) {
            elevTransfer.clearElevTransferLineList();
            int nbr = 1;
            for (BreedingShed x : elevTransfer.getFromCenter().getBreedingShedList()) {
                if (x.getType() == BreedingShedRepository.TYPE_STARTUP && x.getStatus() == BreedingShedRepository.STATUS_BUSY
                        && ((Objects.equals(elevTransfer.getGender(), "M") && x.getMalesNumber() > 0) ||
                        (Objects.equals(elevTransfer.getGender(), "F") && x.getFemalesNumber() > 0))
                ) {
                    ElevTransferLine rline = new ElevTransferLine();
                    rline.setBreedingShed(x);
                    if(Objects.equals(elevTransfer.getGender(), "M")){
                        rline.setExistentNumber(x.getMalesNumber());
                    }
                    else if(Objects.equals(elevTransfer.getGender(), "F")){
                        rline.setExistentNumber(x.getFemalesNumber());
                    }

                    rline.setCurrentDailyAge(x.getDailyAge());
                    rline.setCurrentWeeklyAge(x.getWeeklyAge());

                    if(Objects.equals(elevTransfer.getGender(), "F")) {
                        if(x.getLastTechnicalSheetLine()!= null) {
                                rline.setCurrentWeight(BigDecimal.valueOf(x.getLastTechnicalSheetLine().getFemaleWeight()));
                            }
                            else {
                                rline.setCurrentWeight(BigDecimal.ZERO);

                        }
                    }
                    if(Objects.equals(elevTransfer.getGender(), "M")) {
                        if(x.getLastTechnicalSheetLine() != null){
                            rline.setCurrentWeight(BigDecimal.valueOf(x.getLastTechnicalSheetLine().getMaleWeight()));
                        }
                        else {
                            rline.setCurrentWeight(BigDecimal.ZERO);
                        }
                    }
                    elevTransfer.addElevTransferLineListItem(rline);
                    nbr++;
                }
            }
        }
        response.setValues(elevTransfer);
    }

    @Transactional
    public void elevTransferValidate(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ElevTransfer elevTransfer = request.getContext().asType(ElevTransfer.class);

        if (elevTransfer != null ) {
            elevTransferService.Validate(elevTransfer);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_FOUND));
        }

        response.setValues(elevTransfer);
        response.setReload(true);
    }
    @Transactional
    public void elevTransferCancel(ActionRequest request, ActionResponse response)
            throws AxelorException {
        ElevTransfer elevTransfer = request.getContext().asType(ElevTransfer.class);

        if (elevTransfer != null ) {
            elevTransferService.Cancel(elevTransfer);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.ELEV_TRANSFER_NOT_FOUND));
        }

        response.setValues(elevTransfer);
        response.setReload(true);
    }

}

