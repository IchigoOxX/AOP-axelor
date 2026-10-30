package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.ElevReform;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.gl.apps.pf.service.ContractService;
import com.gl.apps.pf.service.ElevReformService;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class ContractController {
    @Inject
    private ContractService contractService;
    @Transactional
    public void SetVehicleOccupied(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Contract contract = request.getContext().asType(Contract.class);

        if (contract != null ) {
            contractService.SetVehicleOccupied(contract);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.CONTRACT_NOT_FOUND));
        }

        response.setValues(contract);
        response.setReload(true);
    }

    @Transactional
    public void SetVehicleFree(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Contract contract = request.getContext().asType(Contract.class);

        if (contract != null ) {
            contractService.SetVehicleFree(contract);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.CONTRACT_NOT_FOUND));
        }

        response.setValues(contract);
        response.setReload(true);
    }
}

