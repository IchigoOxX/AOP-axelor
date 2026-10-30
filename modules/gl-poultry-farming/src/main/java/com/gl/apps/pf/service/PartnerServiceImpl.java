package com.gl.apps.pf.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.db.Incubator;
import com.gl.apps.pf.db.IncubatorEntry;
import com.gl.apps.pf.db.repo.IncubatorEntryRepository;
import com.gl.apps.pf.db.repo.IncubatorTrolleyRepository;
import com.gl.apps.pf.db.repo.IncubatorsLoadingProgramLineRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDate;
import java.util.List;

public class PartnerServiceImpl implements PartnerService{
    @Inject private PartnerRepository partnerRepository;
    @Inject private AccountRepository accountRepository;
    @Transactional
    public Partner AddComptInfo(Partner partner) throws AxelorException {
        Partner partner1 = partnerRepository.find(partner.getId());
        if (partner1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Partner not found"));
        }
        if(!partner1.getAccountingSituationList().isEmpty()){
            String query = "self.accountType.technicalTypeSelect = ?1 AND self.statusSelect = ?2";

            String technicalTypeSelect = "receivable";
            int statusSelect = 1;
            List<Account> accountList = accountRepository.all()
                    .filter(query, technicalTypeSelect, statusSelect)
                    .fetch();

            if(accountList!= null ){
                if(accountList != null ) {
                    Account customerClient = accountList.get(0);
                    partner1.getAccountingSituationList().get(0).setCustomerAccount(customerClient);
                }
            }
        }

        return partnerRepository.save(partner1);
    }
}
