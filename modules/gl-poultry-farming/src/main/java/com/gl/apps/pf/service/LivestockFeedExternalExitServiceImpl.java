package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.LivestockFeedExternalExit;
import com.gl.apps.pf.db.repo.LivestockFeedExternalExitRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;

public class LivestockFeedExternalExitServiceImpl implements  LivestockFeedExternalExitService {
    @Inject private LivestockFeedExternalExitRepository livestockFeedExternalExitRepository;
    @Inject private StockMoveService stockMoveService;
    @Transactional
    public LivestockFeedExternalExit Validate(LivestockFeedExternalExit livestockFeedExternalExit) throws AxelorException {
        LivestockFeedExternalExit externalExit = livestockFeedExternalExitRepository.find(livestockFeedExternalExit.getId());

        if (externalExit == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_NOT_FOUND));
        }
        if(externalExit.getCustomerDelivery() != null ){
            externalExit.getCustomerDelivery().setLivestockFeedExternalExit(externalExit);
            stockMoveService.realize(externalExit.getCustomerDelivery());
//            externalExit.getCustomerDelivery().setStatusSelect(StockMoveRepository.STATUS_REALIZED);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_CUSTOMER_DELIVERY_NOT_FOUND));
        }

        externalExit.setStatus(LivestockFeedExternalExitRepository.STATUS_DELIVERED);

        return livestockFeedExternalExitRepository.save(externalExit);
    }
    @Transactional
    public LivestockFeedExternalExit Cancel(LivestockFeedExternalExit livestockFeedExternalExit) throws AxelorException {
        LivestockFeedExternalExit externalExit = livestockFeedExternalExitRepository.find(livestockFeedExternalExit.getId());

        if (externalExit == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_NOT_FOUND));
        }
        if(externalExit.getCustomerDelivery() != null ){
            externalExit.getCustomerDelivery().setStatusSelect(StockMoveRepository.STATUS_PLANNED);
        }
        else {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.LIVESTOCK_FEED_EXTERNAL_EXIT_CUSTOMER_DELIVERY_NOT_FOUND));
        }

        externalExit.setStatus(LivestockFeedExternalExitRepository.STATUS_CANCELED);

        return livestockFeedExternalExitRepository.save(externalExit);
    }

}

