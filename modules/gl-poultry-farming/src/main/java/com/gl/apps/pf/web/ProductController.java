package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.service.ProductService;
import com.gl.apps.pf.service.VehicleService;
import com.google.inject.Inject;

public class ProductController {

    @Inject
    private ProductService productService;
    public void SetProductDefaultValues(ActionRequest request, ActionResponse response)
            throws AxelorException {
        Product product = request.getContext().asType(Product.class);

        if (product != null ) {
            productService.SetProductDefaultValues(product);
        }

        response.setValues(product);
        response.setReload(true);
    }
}
