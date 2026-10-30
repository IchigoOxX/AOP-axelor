package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.apps.fleet.db.repo.VehicleRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ContractServiceImpl implements  ContractService {
    @Inject private VehicleRepository vehicleRepository;
    @Inject private ContractRepository contractRepository;
    @Transactional
    public Contract SetVehicleOccupied(Contract contract) throws AxelorException {
        Contract contract1 = contractRepository.find(contract.getId());

        if (contract1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.CONTRACT_NOT_FOUND));
        }
        if(contract1.getVehicle()!= null ) {
            contract1.getVehicle().setOccupied(true);
            vehicleRepository.save(contract1.getVehicle());
        }
    return contractRepository.save(contract1);
    }

    @Transactional
    public Contract SetVehicleFree(Contract contract) throws AxelorException {
        Contract contract1 = contractRepository.find(contract.getId());

        if (contract1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.CONTRACT_NOT_FOUND));
        }
        if(contract1.getVehicle()!= null ) {
            contract1.getVehicle().setOccupied(false);
            vehicleRepository.save(contract1.getVehicle());
        }
        return contractRepository.save(contract1);
    }

}
