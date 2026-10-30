package com.gl.apps.pf.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;

public interface ProductService {
    public Product SetProductDefaultValues(Product product) throws AxelorException;
}
