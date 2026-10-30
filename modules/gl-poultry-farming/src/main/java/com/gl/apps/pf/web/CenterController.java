package com.gl.apps.pf.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.CompanyDepartment;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.Center;
import com.google.inject.Inject;

public class CenterController {

    @Inject
    private StockLocationRepository stockLocationRepo;

    public void onNew(ActionRequest request, ActionResponse response) {
        try {
            Center center = request.getContext().asType(Center.class);
            if (center != null && center.getUnit() == null) {
                User user = AuthUtils.getUser();
                if (user != null && user.getActiveCompany() != null) {
                    Company company = user.getActiveCompany();
                    if (company.getCompanyDepartmentList() != null && !company.getCompanyDepartmentList().isEmpty()) {
                        response.setValue("unit", company.getCompanyDepartmentList().get(0));
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
