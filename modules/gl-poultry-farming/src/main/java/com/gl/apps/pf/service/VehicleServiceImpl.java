// <!--G-Link -->
package com.gl.apps.pf.service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.apps.fleet.db.VehicleFuelLog;
import com.axelor.apps.fleet.db.VehicleOdometer;
import com.axelor.apps.fleet.db.VehicleServiceLog;
import com.axelor.apps.fleet.db.repo.VehicleRepository;
import com.axelor.apps.fleet.db.repo.VehicleServiceLogRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class VehicleServiceImpl implements VehicleService {
    @Inject private VehicleRepository vehicleRepository;
    @Inject private VehicleServiceLogRepository vehicleServiceLogRepository;
    @Transactional
    public Vehicle Vignette(Vehicle vehicle) throws AxelorException {
        Vehicle vehicle1 = vehicleRepository.find(vehicle.getId());

        if (vehicle1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.TECHNICAL_SHEET_NOT_FOUND));
        }
        LocalDate endInsuranceDate = vehicle1.getEndInsuranceDate();
        LocalDate sysDate = LocalDate.now();

        if (endInsuranceDate != null) {
            if (sysDate.isBefore(endInsuranceDate.minusMonths(1))) {
                vehicle1.setIsInsurance(false);
            } else {
                vehicle1.setIsInsurance(true);
            }
        }
        LocalDate endTechnicalVisitDate = vehicle1.getEndTechnicalVisitDate();

        if (endTechnicalVisitDate != null) {
            if (sysDate.isBefore(endTechnicalVisitDate.minusMonths(1))) {
                vehicle1.setIsTechnicalVisit(false);
            } else {
                vehicle1.setIsTechnicalVisit(true);
            }
        }
        String plateNo = vehicle1.getPlateNo();

        int platePart = Integer.parseInt(plateNo.substring(0, 4));

        boolean isEven = platePart % 2 == 0;

        if (isEven) {
            if(vehicle1.getStartVignetteDate()!= null) {
                int nextYear = vehicle1.getStartVignetteDate().getYear() + 1;
                LocalDate lastVignetteDate = LocalDate.of(nextYear, 3, 5); // March 5th
                vehicle1.setLastVignetteDate(lastVignetteDate);
            }

            long daysDifference = ChronoUnit.DAYS.between(sysDate,vehicle1.getLastVignetteDate());

            if (daysDifference <= 15) {
                vehicle1.setIsVignette(true);
            } else {
                vehicle1.setIsVignette(false);
            }
            if(!vehicle1.getVehicleServiceLogList().isEmpty()){
                vehicle1.getVehicleServiceLogList().forEach(line -> {
                    if(line.getDate().getYear() == vehicle1.getLastVignetteDate().getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vignette")){
                        vehicle1.setIsVignette(false);
                    }

                });
            }
        }
        else if (!isEven) {
            if(vehicle1.getStartVignetteDate()!= null) {
                int nextYear = vehicle1.getStartVignetteDate().getYear() + 1;
                LocalDate lastVignetteDate = LocalDate.of(nextYear, 4, 5); // April 5th
                vehicle1.setLastVignetteDate(lastVignetteDate);
            }
            long daysDifference = ChronoUnit.DAYS.between(sysDate, vehicle1.getLastVignetteDate());

            if (daysDifference <= 15) {
                vehicle1.setIsVignette(true);
            } else {
                vehicle1.setIsVignette(false);
            }
            if(!vehicle1.getVehicleServiceLogList().isEmpty()) {
                vehicle1.getVehicleServiceLogList().forEach(line -> {
                    if (line.getDate().getYear() == vehicle1.getLastVignetteDate().getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vignette")) {
                        vehicle1.setIsVignette(false);
                    }
                });
            }
        }

        if(!vehicle1.getVehicleServiceLogList().isEmpty()){
            vehicle1.getVehicleServiceLogList().forEach(line -> {
                if(line.getDate().getYear() == vehicle1.getEndInsuranceDate().getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Assurance")){
                    vehicle1.setFirstInsuranceDate(line.getDate());
                    vehicle1.setEndInsuranceDate(line.getDate().plusYears(1));
                    vehicle1.setIsInsurance(false);
                }
            });
            vehicle1.getVehicleServiceLogList().forEach(line -> {
                if(line.getDate().getYear() == vehicle1.getEndTechnicalVisitDate().getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Visite technique")){
                    vehicle1.setFirstTechnicalVisitDate(line.getDate());
                    vehicle1.setEndTechnicalVisitDate(line.getDate().plusYears(1));
                    vehicle1.setIsTechnicalVisit(false);
                }
            });
            vehicle1.getVehicleServiceLogList().forEach(line -> {
                if(line.getDate().getYear() == vehicle1.getLastVignetteDate().getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vignette")){
                    vehicle1.setStartVignetteDate(line.getDate());
                    if (isEven) {

                        if (vehicle1.getStartVignetteDate() != null) {
                            int nextYear = line.getDate().getYear() + 1;
                            LocalDate lastVignetteDate = LocalDate.of(nextYear, 3, 5); // March 5th
                            vehicle1.setLastVignetteDate(lastVignetteDate);
                        }

                        long daysDifference = ChronoUnit.DAYS.between(vehicle1.getLastVignetteDate(), sysDate);

                        if (daysDifference <= 15) {
                            vehicle1.setIsVignette(true);
                        } else {
                            vehicle1.setIsVignette(false);
                        }
                    }
                    else if (!isEven) {
                        if(vehicle1.getStartVignetteDate()!= null) {
                            int nextYear = line.getDate().getYear() + 1;
                            LocalDate lastVignetteDate = LocalDate.of(nextYear, 3, 6); // April 5th
                            vehicle1.setLastVignetteDate(lastVignetteDate);
                        }
                        long daysDifference = ChronoUnit.DAYS.between(vehicle1.getLastVignetteDate(), sysDate);

                        if (daysDifference <= 15) {
                            vehicle1.setIsVignette(true);
                        } else {
                            vehicle1.setIsVignette(false);
                        }
                    }
                    vehicle1.setIsVignette(false);
                }

            });

        }
        // add new releve kilometrique
        Optional<VehicleServiceLog> maxDateLog = vehicle1.getVehicleServiceLogList().stream()
                .filter(line -> Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vidange"))
                .filter(line -> Boolean.FALSE.equals(line.getIsDone()))
                .max((log1, log2) -> log1.getDate().compareTo(log2.getDate()));
        if(!vehicle1.getVehicleServiceLogList().isEmpty()) {
            if (maxDateLog.isPresent()) {
                VehicleServiceLog maxDateLogValue = maxDateLog.get();
                maxDateLogValue.setIsDone(true);
                VehicleOdometer vehicleOdometer = new VehicleOdometer();
                vehicleOdometer.setVehicle(vehicle1);
                vehicleOdometer.setDate(maxDateLogValue.getDate());
                vehicleOdometer.setValue(maxDateLogValue.getKilometrage());
                vehicle1.addVehicleOdometerListItem(vehicleOdometer);
                vehicle1.setVehicleOdometer(maxDateLogValue.getKilometrage());
                vehicle1.setVehicleOilChange(false);
                vehicleRepository.save(vehicle1);
            }
        }
        // vidange

        if(vehicle1.getVehicleServiceLogList().isEmpty() || (!vehicle1.getVehicleServiceLogList().isEmpty() && vehicle1.getVehicleServiceLogList().stream()
                .noneMatch(line1 -> Objects.equals(line1.getVehicleServiceList().get(0).getServiceType().getName(), "Vidange")))) {
            vehicle1.getVehicleOdometerList().forEach(line -> {
                  List<VehicleOdometer> fuelLogs = vehicle1.getVehicleOdometerList();
                  int size = fuelLogs.size();
                  VehicleOdometer latestLog = fuelLogs.get(size - 1);
                  VehicleOdometer previousLog = fuelLogs.get(0);
                  BigDecimal kilometerage = latestLog.getValue().subtract(previousLog.getValue());
                  BigDecimal diff = new BigDecimal("10000").subtract(kilometerage);
                  vehicle1.setDiffVehicleOdometer(diff);

           });
            if (vehicle1.getDiffVehicleOdometer().compareTo(BigDecimal.ZERO) <= 0) {
                vehicle1.setVehicleOilChange(true);
                vehicle1.setDiffVehicleOdometer(BigDecimal.ZERO);
            }
            else {
                vehicle1.setVehicleOilChange(false);
            }
        }
        else {
            Optional<VehicleServiceLog> maxDateLogg = vehicle1.getVehicleServiceLogList().stream()
                    .filter(line -> Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vidange"))
                    .filter(line -> Boolean.TRUE.equals(line.getIsDone()))
                    .max((log1, log2) -> log1.getDate().compareTo(log2.getDate()));
            if (!vehicle1.getVehicleServiceLogList().isEmpty() && maxDateLogg.isPresent()) {
                VehicleServiceLog maxDateLogValue = maxDateLogg.get();

                // Get the max date from the list of vehicle odometer readings that matches the service type "Vidange"
                List<VehicleOdometer> vehicleOdometerList = vehicle1.getVehicleOdometerList();
                BigDecimal maxDateOdometerValue = vehicleOdometerList.stream()
                        .filter(odometer -> odometer.getDate().equals(maxDateLogValue.getDate()))
                        .map(VehicleOdometer::getValue)
                        .max(Comparator.naturalOrder())
                        .orElse(BigDecimal.ZERO);

                // Get the latest odometer reading before the service date
                BigDecimal latestOdometerValue = vehicleOdometerList.stream()
                        .filter(odometer -> odometer.getDate().isAfter(maxDateLogValue.getDate()))
                        .map(VehicleOdometer::getValue)
                        .max(Comparator.naturalOrder())
                        .orElse(BigDecimal.ZERO);
                if (!latestOdometerValue.equals(BigDecimal.ZERO)) {
                    //hereeee
                    vehicle1.setVehicleOilChange(true);
                    // Calculate the difference in kilometers
                    BigDecimal kilometerage = latestOdometerValue.subtract(maxDateOdometerValue);

                    // Set the difference in the vehicle object
                    BigDecimal diff = new BigDecimal("10000").subtract(kilometerage);
                    vehicle1.setDiffVehicleOdometer(diff);
                    // Check if oil change is needed
                    if (diff.compareTo(BigDecimal.ZERO) <= 0) {
                        vehicle1.setVehicleOilChange(true);
                        vehicle1.setDiffVehicleOdometer(BigDecimal.ZERO);
                    } else {
                        vehicle1.setVehicleOilChange(false);
                    }
                }
            }
        }
        if(vehicle1.getVehicleOdometerList().isEmpty()){
            vehicle1.setVehicleOilChange(false);
        }

//        if (!vehicle1.getVehicleOdometerList().isEmpty()) {
//            List<VehicleOdometer> fuelLogs = vehicle1.getVehicleOdometerList();
//            int size = fuelLogs.size();
//
//            if (size >= 2) {
//                VehicleOdometer latestLog = fuelLogs.get(size - 1);
//                VehicleOdometer previousLog = fuelLogs.get(size - 2);
//
//                BigDecimal kilometerage = latestLog.getValue().subtract(previousLog.getValue());
//                vehicle1.setDiffVehicleOdometer(kilometerage);
//                if (kilometerage.compareTo(new BigDecimal("10000")) >= 0) {
//                    vehicle1.setVehicleOilChange(true);
//                } else {
//                    vehicle1.setVehicleOilChange(false);
//                }
//            }
//            else {
//                vehicle1.setDiffVehicleOdometer(BigDecimal.ZERO);
//                vehicle1.setVehicleOilChange(false);
//            }
//        }

//        if(!vehicle1.getVehicleServiceLogList().isEmpty()) {
//            vehicle1.getVehicleServiceLogList().forEach(line -> {
//                if (line.getDate().getYear() == sysDate.getYear() && Objects.equals(line.getVehicleServiceList().get(0).getServiceType().getName(), "Vidange")) {
//                    vehicle1.setDiffVehicleOdometer(BigDecimal.ZERO);
//                    vehicle1.setVehicleOilChange(false);
//                }
//
//            });
//        }

        return vehicleRepository.save(vehicle1);
    }
}
