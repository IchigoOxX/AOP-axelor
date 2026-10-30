package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.utils.StockLocationUtilsService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.*;
import com.axelor.apps.stock.service.StockMoveService;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.ZonedDateTime;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class HatcherEntryServiceImpl implements HatcherEntryService {
    @Inject
    private HatcherEntryRepository hatcherEntryRepository;
    @Inject
    private StockMoveService stockMoveService;
    @Inject private HatcherExitRepository hatcherExitRepository;
    @Inject private StockLocationUtilsService stockLocationService;
    @Transactional
    public HatcherEntry Validate(HatcherEntry hatcherEntry) throws AxelorException {

        HatcherEntry hatcherEntry1 = hatcherEntryRepository.find(hatcherEntry.getId());

        if (hatcherEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_FOUND));
        }

        if(hatcherEntry1.getHatchery()!= null){
            if(hatcherEntry1.getHatchery().getUnit() != null) {
                hatcherEntry1.setUnit(hatcherEntry1.getHatchery().getUnit());
            }
            else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No unit was specified for hatchery: "+ hatcherEntry1.getHatchery().getName()));
            }
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHERY_NOT_SPECIFIED));
        }

        if (!hatcherEntry1.getHatcherEntryLinesList().isEmpty()) {
            hatcherEntry1.getHatcherEntryLinesList().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_BUSY);
                        line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                        line.setStatus(HatcherTrolleyRepository.STATUS_IN_THE_HATCHER);
                    }
                });
        }

        hatcherEntry1.getIncubatorExit().setStatus(IncubatorExitRepository.STATUS_EXECUTED);
        // Realize the stock move
        StockMove stockMove = null;

        if (hatcherEntry1.getIncubatorExit() != null) {
            stockMove = hatcherEntry1.getIncubatorExit().getDispatchStock();
            stockMove.setHatcherEntry(hatcherEntry1);
            stockMove.setSubType(StockMoveRepository.SUB_TYPE_HATCHER_ENTRIES);
        }

        if (stockMove != null) {
            stockMove.setRealDate(LocalDate.now());
            //stockMoveService.realize(stockMove);
        }
        hatcherEntry1.setStatus(HatcherEntryRepository.STATUS_EXECUTING);

        // Create new hatcher exit
        HatcherExit hatcherExit = new HatcherExit();
        hatcherExit.setDateTimeHatcherEntry(hatcherEntry1.getDateTime());
        hatcherExit.setBreed(hatcherEntry1.getBreed());
        if (hatcherEntry1.getHatcherEntryLinesList() != null && !hatcherEntry1.getHatcherEntryLinesList().isEmpty()) {
            String hatcherReferences = hatcherEntry1.getHatcherEntryLinesList().stream()
                    .map(line -> line.getHatcher().getReference())
                    .collect(Collectors.joining(", "));
            hatcherExit.setHatcher(hatcherReferences);
        }

        hatcherExit.setIncubatorExit(hatcherEntry1.getIncubatorExit());
        hatcherExit.setDateTimeIncubatorExit(hatcherEntry1.getDateTimeIncubatorExitEntry());
        hatcherExit.setIncubator(hatcherEntry1.getIncubator());
        hatcherExit.setHatchery(hatcherEntry1.getHatchery());
        hatcherExit.setStatus(HatcherExitRepository.STATUS_NOT_EXECUTED);
        hatcherExit.setDatetime(LocalDateTime.now());
        hatcherExit.setHatcherEntry(hatcherEntry1);

        int totalEntry = hatcherEntry1
                .getHatcherEntryLinesList().stream()
                .mapToInt(t-> t.getNumberOfOac().intValue()).sum();
        hatcherExit.setTotalEntry(totalEntry);


        if (hatcherEntry1.getHatchery() != null && hatcherEntry1.getHatchery().getPackagingProduct() != null) {
            hatcherExit.setNumberOfChicksPerPackage(hatcherEntry1.getHatchery().getPackagingProduct().getCapacity());
        }

        if (hatcherEntry1.getIncubatorExit() != null && hatcherEntry1.getIncubatorExit().getIncubatorEntry() != null) {
            hatcherExit.setLoadingProgramLine(hatcherEntry1.getIncubatorExit().getIncubatorEntry().getLoadingProgramLine());
        }

        if (hatcherEntry1.getBreed() != null) {
            hatcherExit.setSuccessRate(hatcherEntry1.getBreed().getHatchingRate());
        }
        if (!hatcherEntry1.getHatcherEntryLinesList().isEmpty()) {
            for (HatcherEntryLine x : hatcherEntry1.getHatcherEntryLinesList()) {
                HatcherExitLine rline = new HatcherExitLine();
                rline.setIncubatorTrolley(x.getIncubatorTrolley());
                rline.setHatcherTrolley(x.getHatcherTrolley());
                rline.setBreedingShed(x.getBreedingShed());
                rline.setCenter(x.getCenter());
                rline.setFirstLoadingNumber(x.getNumberOfOac());
                rline.setStatus(x.getStatus());
                rline.setCurrentDailyAge(x.getCurrentDailyAge());
                rline.setCurrentWeeklyAge(x.getCurrentWeeklyAge());
                rline.setTrackingNumber(x.getTrackingNumber());
                rline.setLoadingType(x.getLoadingType());
                rline.setHatcher(x.getHatcher());
                hatcherExit.setCapacity(x.getNumberOfOac());
                hatcherExit.addHatcherExitLinesListItem(rline);
            }
            hatcherExit.setDateTimeHatcherEntryExit(hatcherExit.getDateTimeIncubatorExit().plusDays(hatcherEntry1.getHatchery().getNumberOfHatchingDays()).plusHours(hatcherEntry1.getHatchery().getNumberOfHoursOfHatching()));
        }
        hatcherExitRepository.save(hatcherExit);

        return hatcherEntryRepository.save(hatcherEntry1);
    }

    @Transactional
    public HatcherEntry Cancel(HatcherEntry hatcherEntry) throws AxelorException {

        HatcherEntry hatcherEntry1 = hatcherEntryRepository.find(hatcherEntry.getId());

        if (hatcherEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_FOUND));
        }
        hatcherEntry1.setReasonOfCancel(hatcherEntry.getReasonOfCancel());
        if (!hatcherEntry1.getHatcherEntryLinesList().isEmpty()) {
            hatcherEntry1.getHatcherEntryLinesList().forEach(
                line -> {
                    if (line != null) {
                        line.getIncubatorTrolley().setStatus(IncubatorTrolleyRepository.STATUS_IN_THE_INCUBATOR);
                        line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_BUSY);
                        line.setStatus(HatcherTrolleyRepository.STATUS_BUSY);
                    }
                });

        }

        //hatcherEntry1.getIncubatorExit().setStatus(IncubatorExitRepository.STATUS_CANCELED);
        StockMove stockMove = hatcherEntry1.getIncubatorExit().getDispatchStock();

        if (stockMove != null) {
            stockMoveService.cancel(stockMove);
        }
        hatcherEntry1.setStatus(HatcherEntryRepository.STATUS_CANCELED);

        return hatcherEntryRepository.save(hatcherEntry1);
    }

    @Transactional
    public HatcherEntry Revalidate(HatcherEntry hatcherEntry) throws AxelorException {
        HatcherEntry hatcherEntry1 = hatcherEntryRepository.find(hatcherEntry.getId());

        if (hatcherEntry1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_NOT_FOUND));
        }

        if (!hatcherEntry1.getHatcherEntryLinesList().isEmpty()) {
            for (HatcherEntryLine line : hatcherEntry1.getHatcherEntryLinesList()) {
                if (line != null) {
                    if (line.getHatcherTrolley().getStatus() != HatcherTrolleyRepository.STATUS_RESERVED) {
                        line.getHatcherTrolley().setStatus(HatcherTrolleyRepository.STATUS_RESERVED);
                        line.setStatus(HatcherTrolleyRepository.STATUS_RESERVED);
                    } else
                        throw new AxelorException(
                                TraceBackRepository.CATEGORY_MISSING_FIELD,
                                I18n.get(PoultryFarmingExceptionMessage.HATCHER_ENTRY_TROLLEY_IS_RESERVED));

                }
            }
        }

        StockMove stockMove = hatcherEntry1.getIncubatorExit().getDispatchStock();

        if (stockMove != null) {
            for (StockMoveLine line : stockMove.getStockMoveLineList()) {
                BigDecimal qty = stockLocationService.getRealQtyOfProductInStockLocations(line.getProduct().getId(), java.util.Collections.singletonList(stockMove.getFromStockLocation().getId()), AuthUtils.getUser().getActiveCompany().getId());
                    if (!line.getProduct().getAllowNegativeStock()) {
                        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new AxelorException(
                                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                                    I18n.get(line.getProductName() + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + " does not authorize negative quantity or has quantity equal or less than 0 in the location : " + stockMove.getFromStockLocation().getName() + " !")
                            );
                        }
                    }
            }
            stockMoveService.goBackToDraft(stockMove);
            stockMoveService.plan(stockMove);
        }

        hatcherEntry1.setStatus(HatcherEntryRepository.STATUS_NOT_EXECUTED);
        return hatcherEntryRepository.save(hatcherEntry1);
    }
}





