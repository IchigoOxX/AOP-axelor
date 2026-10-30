/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.gl.apps.pf.module;

import com.axelor.app.AxelorModule;
import com.gl.apps.pf.db.FleshMovement;
import com.gl.apps.pf.db.repo.*;
import com.gl.apps.pf.service.*;

public class PoultryFarmingModule extends AxelorModule {

  @Override
  protected void configure() {

    // bind of repository
    bind(IncubatorsLoadingProgramLineRepository.class).to(IncubatorsLoadingProgramLineRepo.class);
    bind(IncubatorsLoadingProgramRepository.class).to(IncubatorsLoadingProgramRepo.class);
    bind(HatcheryEggsReceptionLineRepository.class).to(HatcheryEggsReceptionLineRepo.class);
    bind(HatcheryEggsReceptionRepository.class).to(HatcheryEggsReceptionRepo.class);
    bind(IncubatorTrolleysChargingLineRepository.class).to(IncubatorTrolleysChargingLineRepo.class);
    bind(IncubatorTrolleysChargingRepository.class).to(IncubatorTrolleysChargingRepo.class);
    bind(BreedRepository.class).to(BreedRepo.class);
    bind(HatcheryRepository.class).to(HatcheryRepo.class);
    bind(CenterRepository.class).to(CenterRepo.class);
    bind(BreedingShedRepository.class).to(BreedingShedRepo.class);
    bind(ReproProductionRepository.class).to(ReproProductionRepo.class);
    bind(ReproProductionLineRepository.class).to(ReproProductionLineRepo.class);
    bind(StartTechnicalSheetRepository.class).to(StartTechnicalSheetRepo.class);
    bind(StartReceivingChicksRepository.class).to(StartReceivingChicksRepo.class);
    bind(ElevTransferRepository.class).to(ElevTransferRepo.class);
    bind(ReceptionReproProductionRepository.class).to(ReceptionReproProductionRepo.class);
    bind(PharmacyTreatmentRepository.class).to(PharmacyTreatmentRepo.class);
    bind(StartElevReformRepository.class).to(StartElevReformRepo.class);
    bind(SlaughterhouseRepository.class).to(SlaughterhouseRepo.class);
    bind(StartElevMovementRepository.class).to(StartElevMovementRepo.class);
    bind(LivestockFeedFactoryRepository.class).to(LivestockFeedFactoryRepo.class);
    bind(ProductOrderRepository.class).to(ProductOrderRepo.class);
    bind(ProductConsumptionRepository.class).to(ProductConsumptionRepo.class);
    bind(LogisticsSheetRepository.class).to(LogisticsSheetRepo.class);
    bind(ReproTechnicalSheetRepository.class).to(ReproTechnicalSheetRepo.class);
    bind(ReproElevReformRepository.class).to(ReproElevReformRepo.class);
    bind(TechnicalSheetRepository.class).to(TechnicalSheetRepo.class);
    bind(TechnicalSheetLineRepository.class).to(TechnicalSheetLineRepo.class);
    bind(ElevReformRepository.class).to(ElevReformRepo.class);
    bind(IncubatorEntryRepository.class).to(IncubatorEntryRepo.class);
    bind(IncubatorExitRepository.class).to(IncubatorExitRepo.class);
    bind(HatcherEntryRepository.class).to(HatcherEntryRepo.class);
    bind(HatcherExitRepository.class).to(HatcherExitRepo.class);
    bind(HatcheryEggExitRepository.class).to(HatcherEggExitRepo.class);
    bind(HatcheryDeliverySaleRepository.class).to(HatcheryDeliverySaleRepo.class);
    bind(FleshLoadingProgramLineRepository.class).to(FleshLoadingProgramLineRepo.class);
    bind(FleshLoadingProgramRepository.class).to(FleshLoadingProgramRepo.class);
    bind(FleshChicksReceptionRepository.class).to(FleshChicksReceptionRepo.class);
    bind(FleshMovementRepository.class).to(FleshMovementRepo.class);
    bind(LivestockFeedProductionRepository.class).to(LivestockFeedProductionRepo.class);
    bind(LivestockFeedExternalExitRepository.class).to(LivestockFeedExternalExitRepo.class);
    bind(MaintenanceEnergyConsumptionRepository.class).to(MaintenanceEnergyConsumptionRepo.class);

    // bind of services
    bind(IncubatorTrolleysChargingService.class).to(IncubatorTrolleysChargingServiceImpl.class);
    bind(IncubatorsLoadingProgramService.class).to(IncubatorsLoadingProgramServiceImpl.class);
    bind(HatcheryService.class).to(HatcheryServiceImpl.class);
    bind(HatcherService.class).to(HatcherServiceImpl.class);
    bind(HatcheryEggsReceptionService.class).to(HatcheryEggsReceptionServiceImpl.class);
    bind(ReproProductionService.class).to(ReproProductionServiceImpl.class);
    bind(BreedingShedService.class).to(BreedingShedServiceImpl.class);
    bind(StartReceivingChicksService.class).to(StartReceivingChicksServiceImp.class);
    bind(StartTechnicalSheetService.class).to(StartTechnicalSheetServiceImp.class);
    bind(StockMoveService.class).to(StockMoveServiceImp.class);
    bind(ElevTransferService.class).to(ElevTransferServiceImpl.class);
    bind(ReceptionReproProductionService.class).to(ReceptionReproProductionServiceImpl.class);
    bind(StartElevReformService.class).to(StartElevReformServiceImpl.class);
    bind(StartElevMovementService.class).to(StartElevMovementServiceImpl.class);
    bind(PharmacyTreatmentService.class).to(PharmacyTreatmentSeriveImpl.class);
    bind(ProductOrderService.class).to(ProductOrderServiceImpl.class);
    bind(ProductConsumptionService.class).to(ProductConsumptionServiceImpl.class);
    bind(LogisticsSheetService.class).to(LogisticsSheetServiceImpl.class);
    bind(ReproTechnicalSheetService.class).to(ReproTechnicalSheetServiceImpl.class);
    bind(ReproElevReformService.class).to(ReproElevReformServiceImpl.class);
    bind(ReproElevMovementService.class).to(ReproElevMovementServiceImpl.class);
    bind(TechnicalSheetService.class).to(TechnicalSheetServiceImpl.class);
    bind(ElevReformService.class).to(ElevReformServiceImpl.class);
    bind(VehicleService.class).to(VehicleServiceImpl.class);
    bind(IncubatorEntryService.class).to(IncubatorEntryServiceImpl.class);
    bind(IncubatorExitService.class).to(IncubatorExitServiceImpl.class);
    bind(HatcherEntryService.class).to(HatcherEntryServiceImpl.class);
    bind(HatcherExitService.class).to(HatcherExitServiceImpl.class);
    bind(IncubatorService.class).to(IncubatorServiceImpl.class);
    bind(HatcheryEggExitService.class).to(HatcheryEggExitServiceImpl.class);
    bind(HatcheryDeliverySaleService.class).to(HatcheryDeliverySaleServiceImpl.class);
    bind(FleshLoadingProgramService.class).to(FleshLoadingProgramServiceImpl.class);
    bind(FleshChicksReceptionService.class).to(FleshChicksReceptionServiceImpl.class);
    bind(FleshMovementService.class).to(FleshMovementServiceImpl.class);
    bind(LivestockFeedProductionService.class).to(LivestockFeedProductionServiceImpl.class);
    bind(LivestockFeedExternalExitService.class).to(LivestockFeedExternalExitServiceImpl.class);
    //bind(CenterService.class).to(CenterServiceImpl.class);
    bind(MaintenanceEnergyConsumptionService.class).to(MaintenanceEnergyConsumptionServiceImpl.class);
    bind(MaintenanceMachineService.class).to(MaintenanceMachineServiceImpl.class);
    bind(PartnerService.class).to(PartnerServiceImpl.class);
    bind(ProductService.class).to(ProductServiceImpl.class);
    bind(ContractService.class).to(ContractServiceImpl.class);
    bind(InvoiceService.class).to(InvoiceServiceImpl.class);
    bind(IncubatorTrolleyService.class).to(IncubatorTrolleyServiceImpl.class);
  }
}
