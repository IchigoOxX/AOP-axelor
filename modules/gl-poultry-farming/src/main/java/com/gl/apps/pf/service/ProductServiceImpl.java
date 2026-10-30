package com.gl.apps.pf.service;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductServiceImpl implements ProductService {
    @Inject private ProductRepository productRepository;

    @Transactional
    public Product SetProductDefaultValues(Product product) throws AxelorException {
        Product product1 = productRepository.find(product.getId());

        if (product1 == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_NOT_FOUND));
        }
//        if(product1.getAccountManagementList().isEmpty()) {


//            throw new AxelorException(
//                    TraceBackRepository.CATEGORY_MISSING_FIELD,
//                    I18n.get(PoultryFarmingExceptionMessage.PRODUCT_CONFIG_NOT_SPECIFIED));
//        }
        if(product1.getProductDefaultConfiguration()!= null ) {
            if(product1.getAccountManagementList().isEmpty()) {
                AccountManagement accountManagement = new AccountManagement();
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */

                product1.addAccountManagementListItem(accountManagement);
            }
            else {
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* accountManagement setting removed */
                /* Product configuration copying removed */
            }
        }

        return productRepository.save(product1);
    }

}


