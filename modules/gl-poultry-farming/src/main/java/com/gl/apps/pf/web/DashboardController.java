package com.gl.apps.pf.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CompanyDepartment;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.gl.apps.pf.db.*;
import com.gl.apps.pf.db.repo.HatcherExitRepository;
import com.gl.apps.pf.db.repo.HatcheryEggsReceptionLineRepository;
import com.gl.apps.pf.db.repo.ProductConsumptionLineRepository;
import com.gl.apps.pf.exception.PoultryFarmingExceptionMessage;
import com.google.inject.Inject;

import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DashboardController {
    @Inject
    private HatcheryEggsReceptionLineRepository hatcheryEggsReceptionLineRepository;
    @Inject
    private HatcherExitRepository hatcherExitRepository;
    @Inject
    private StockMoveLineRepository stockMoveLineRepository;
    @Inject
    private ProductConsumptionLineRepository productConsumptionLineRepository;

    @Transactional
    public void getLinesEggsPerCenter(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardRepro> dashboardProd = new ArrayList<>();
        Map<Long, DashboardRepro> centerDashboardMap = new HashMap<>();
        CompanyDepartment unitMap = (CompanyDepartment) request.getContext().get("secondUnit");
        if(unitMap == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }
        Long unitId = unitMap.getId();

        String startDate = request.getContext().get("secondStartDate").toString();
        String endDate = request.getContext().get("secondEndDate").toString();

        for (HatcheryEggsReceptionLine hatcheryEggsReceptionLine : hatcheryEggsReceptionLineRepository.all()
                .filter("self.eggsReception.status = 2 AND self.eggsReception.unit.id = ? AND self.eggsReception.dateTime between ? and ?", unitId, startDate, endDate).fetch()) {

            BreedingShed breedingShed = hatcheryEggsReceptionLine.getBreedingShed();
            Center center = breedingShed.getCenter();

            Long centerId = center.getId();
            TrackingNumber trackingNumber = hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber();

            Integer sumHe = hatcheryEggsReceptionLine.getReceivedNumberOfHe();
            Integer sumDye = hatcheryEggsReceptionLine.getReceivedNumberOfDye();
            Integer sumDe = hatcheryEggsReceptionLine.getReceivedNumberOfDe();

            DashboardRepro dashboardRepro = centerDashboardMap.get(centerId);

            if (dashboardRepro == null) {
                dashboardRepro = new DashboardRepro();
                dashboardRepro.setCenter(center);
                dashboardRepro.setTrackingNumber(trackingNumber);
                dashboardRepro.setHe(0);
                dashboardRepro.setDye(0);
                dashboardRepro.setDe(0);
                dashboardRepro.setTotal(0);
            }

            dashboardRepro.setHe(dashboardRepro.getHe() + sumHe);
            dashboardRepro.setDye(dashboardRepro.getDye() + sumDye);
            dashboardRepro.setDe(dashboardRepro.getDe() + sumDe);
            dashboardRepro.setTotal(dashboardRepro.getTotal() + sumHe + sumDye + sumDe);

            centerDashboardMap.put(centerId, dashboardRepro);
        }

        dashboardProd.addAll(centerDashboardMap.values());

        response.setValue("eggsPerCenterList", dashboardProd);
    }

    @Transactional
    public void getLinesEggsPerBreedingShed(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardRepro> dashboardProd = new ArrayList<>();

        String startDate = request.getContext().get("thirdStartDate").toString();
        String endDate = request.getContext().get("thirdEndDate").toString();
        CompanyDepartment unitMap = (CompanyDepartment) request.getContext().get("thirdUnit");
        if(unitMap == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }
        Long unitId = unitMap.getId();


        for (HatcheryEggsReceptionLine hatcheryEggsReceptionLine : hatcheryEggsReceptionLineRepository.all()
                .filter("self.eggsReception.status = 2 AND self.eggsReception.unit.id = ? AND self.eggsReception.dateTime between ? and ?", unitId, startDate, endDate).fetch()) {

            BreedingShed breedingShed = hatcheryEggsReceptionLine.getBreedingShed();
            Center center = hatcheryEggsReceptionLine.getEggsReception().getCenter();
            TrackingNumber trackingNumber = hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber();

            Integer sumHe = hatcheryEggsReceptionLine.getReceivedNumberOfHe();
            Integer sumDye = hatcheryEggsReceptionLine.getReceivedNumberOfDye();
            Integer sumDe = hatcheryEggsReceptionLine.getReceivedNumberOfDe();

            boolean found = false;

            for (DashboardRepro dashboardRepro : dashboardProd) {
                if (dashboardRepro.getBreedingShed().equals(breedingShed) && dashboardRepro.getCenter().equals(center) && dashboardRepro.getTrackingNumber().equals(trackingNumber)) {
                    dashboardRepro.setHe(dashboardRepro.getHe() + sumHe);
                    dashboardRepro.setDye(dashboardRepro.getDye() + sumDye);
                    dashboardRepro.setDe(dashboardRepro.getDe() + sumDe);
                    dashboardRepro.setTotal(dashboardRepro.getDe() + dashboardRepro.getDye() + dashboardRepro.getHe());

                    found = true;
                    break;
                }
            }

            if (!found) {
                DashboardRepro dashboardRepro = new DashboardRepro();
                dashboardRepro.setBreedingShed(breedingShed);
                dashboardRepro.setCenter(center);
                dashboardRepro.setTrackingNumber(trackingNumber);
                dashboardRepro.setHe(sumHe);
                dashboardRepro.setDye(sumDye);
                dashboardRepro.setDe(sumDe);
                dashboardRepro.setTotal(sumHe + sumDye + sumDe);

                dashboardProd.add(dashboardRepro);
            }
        }

        response.setValue("eggsPerBreedingShedList", dashboardProd);
    }

    @Transactional
    public void getLinesEggsPerTrackingNumberCenter(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardRepro> dashboardProd = new ArrayList<>();
        Map<String, DashboardRepro> centerTrackingDashboardMap = new HashMap<>();

        String startDate = request.getContext().get("fourthStartDate").toString();
        String endDate = request.getContext().get("fourthEndDate").toString();
        CompanyDepartment unitMap = (CompanyDepartment) request.getContext().get("fourthUnit");
        if(unitMap== null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }
        Long unitId = unitMap.getId();

        for (HatcheryEggsReceptionLine hatcheryEggsReceptionLine : hatcheryEggsReceptionLineRepository.all()
                .filter("self.eggsReception.status = 2 AND self.eggsReception.unit.id = ? AND self.eggsReception.dateTime between ? and ?", unitId, startDate, endDate)
                .fetch()) {

            BreedingShed breedingShed = hatcheryEggsReceptionLine.getBreedingShed();
            Center center = breedingShed.getCenter();
            TrackingNumber trackingNumber = hatcheryEggsReceptionLine.getEggsReception().getTrackingNumber();

            Long centerId = center.getId();
            String key = centerId + "-" + trackingNumber.getId();

            Integer sumHe = hatcheryEggsReceptionLine.getReceivedNumberOfHe();
            Integer sumDye = hatcheryEggsReceptionLine.getReceivedNumberOfDye();
            Integer sumDe = hatcheryEggsReceptionLine.getReceivedNumberOfDe();

            DashboardRepro dashboardRepro = centerTrackingDashboardMap.get(key);

            if (dashboardRepro == null) {
                dashboardRepro = new DashboardRepro();
                dashboardRepro.setCenter(center);
                dashboardRepro.setTrackingNumber(trackingNumber);
                dashboardRepro.setHe(0);
                dashboardRepro.setDye(0);
                dashboardRepro.setDe(0);
                dashboardRepro.setTotal(0);
            }

            dashboardRepro.setHe(dashboardRepro.getHe() + sumHe);
            dashboardRepro.setDye(dashboardRepro.getDye() + sumDye);
            dashboardRepro.setDe(dashboardRepro.getDe() + sumDe);
            dashboardRepro.setTotal(dashboardRepro.getTotal() + sumHe + sumDye + sumDe);

            centerTrackingDashboardMap.put(key, dashboardRepro);
        }

        dashboardProd.addAll(centerTrackingDashboardMap.values());

        response.setValue("eggsPerCenterPerTrackingNumberList", dashboardProd);
    }

    @Transactional
    public void getLinesChicksProductionSales(ActionRequest request, ActionResponse response) throws AxelorException {
        List<ChicksDashboard> dashboardChicks = new ArrayList<>();
        BigDecimal sumUnitPrice = BigDecimal.ZERO;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String productName = null;
        Unit productUnit = null;
        Product product = null;

        String startDate = request.getContext().get("fifthStartDate").toString();
        String endDate = request.getContext().get("fifthEndDate").toString();
        CompanyDepartment companyDepartment = (CompanyDepartment) request.getContext().get("fifthUnit");
        if(companyDepartment == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }
        Long unitId = companyDepartment.getId();

        for (HatcherExit hatcherExit : hatcherExitRepository.all()
                .filter("self.status = 2 AND self.unit.id = ? AND self.datetime BETWEEN ? AND ?", unitId, startDate, endDate).fetch()) {
            if (hatcherExit.getCustomerSaleOrderLine() != null) {
                if (!hatcherExit.getCustomerSaleOrderLine().isEmpty()) {
                    productName = hatcherExit.getCustomerSaleOrderLine().get(0).getProductName();
                    productUnit = hatcherExit.getCustomerSaleOrderLine().get(0).getUnit();
                    product = hatcherExit.getCustomerSaleOrderLine().get(0).getProduct();
                } else {
                    throw new AxelorException(
                            TraceBackRepository.CATEGORY_MISSING_FIELD,
                            I18n.get("Sale order line list is empty for customer sale order  " + hatcherExit.getCustomerSaleOrderLine().get(0).getSaleOrder().getFullName()));
                }

            } else {
                throw new AxelorException(
                        TraceBackRepository.CATEGORY_MISSING_FIELD,
                        I18n.get("No customer sale order was not specified for " + hatcherExit.getReference()));
            }


            for (SaleOrderLine saleOrderLine : hatcherExit.getCustomerSaleOrderLine()) {
                sumUnitPrice = sumUnitPrice.add(saleOrderLine.getPrice());
            }
            int size = hatcherExit.getCustomerSaleOrderLine().size();
            BigDecimal avg = sumUnitPrice.divide(BigDecimal.valueOf(size));

            Integer internNumber = hatcherExit.getInternNumber();
            Integer externNumber = hatcherExit.getExternNumber();

            boolean found = false;
            for (ChicksDashboard dashboardChick : dashboardChicks) {
                if (dashboardChick.getProductName().equals(productName)) {
                    dashboardChick.setProducedQty(dashboardChick.getProducedQty() + internNumber + externNumber);
                    dashboardChick.setSaleQty(dashboardChick.getSaleQty() + externNumber);
                    dashboardChick.setSaleAverageUnitPrice(avg);
                    dashboardChick.setSaleAmount(avg.multiply(BigDecimal.valueOf(dashboardChick.getSaleQty())));
                    dashboardChick.setQty(dashboardChick.getQty() + internNumber);
                    dashboardChick.setProducedCostUnitPrice(product.getCostPrice());
                    dashboardChick.setProducedAmount(product.getCostPrice().multiply(BigDecimal.valueOf(dashboardChick.getProducedQty())));

                    found = true;
                    break;
                }
            }

            if (!found) {
                ChicksDashboard chicksDashboard = new ChicksDashboard();
                chicksDashboard.setProductName(productName);
                chicksDashboard.setProductUnit(productUnit);
                chicksDashboard.setProducedQty(internNumber + externNumber);
                chicksDashboard.setSaleQty(externNumber);
                chicksDashboard.setSaleAverageUnitPrice(avg);
                chicksDashboard.setSaleAmount(avg.multiply(BigDecimal.valueOf(chicksDashboard.getSaleQty())));
                chicksDashboard.setQty(internNumber);
                chicksDashboard.setProducedCostUnitPrice(product.getCostPrice());
                chicksDashboard.setProducedAmount(product.getCostPrice().multiply(BigDecimal.valueOf(chicksDashboard.getProducedQty())));

                dashboardChicks.add(chicksDashboard);
            }
        }

        response.setValue("chicksProductionSalesList", dashboardChicks);
    }

    @Transactional
    public void getLinesAliments(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardAliments> alimentsDashboardList = new ArrayList<>();

        String startDate = request.getContext().get("firstStartDate").toString();
        String endDate = request.getContext().get("firstEndDate").toString();
        Map<String, Object> productCategoryMap = (Map<String, Object>) request.getContext().get("productCategory");
        if(productCategoryMap == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the product category !")
            );
        }

        Map<String, Object> stockMoveLocMap = (Map<String, Object>) request.getContext().get("stockLocation");
        if(stockMoveLocMap  == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the stock location !")
            );
        }

        CompanyDepartment unitMap = (CompanyDepartment) request.getContext().get("firstUnit");
        if(unitMap == null ){
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }

        Integer categoryProductId = (Integer) productCategoryMap.get("id");
        Integer stockLocationId = (Integer) stockMoveLocMap.get("id");
        Long unitId = unitMap.getId();

        List<StockMoveLine> stockMoveLines = stockMoveLineRepository.all()
                .filter("self.stockMove.statusSelect = 3 " +
                                "AND self.product.productCategory.id = ? " +
                                "AND self.stockMove.realDate BETWEEN ? and ?" +
                                "AND (self.fromStockLocation.id = ? OR self.toStockLocation.id = ? )" +
                                "AND (self.fromStockLocation.unit.id = ? OR self.toStockLocation.unit.id = ?)", categoryProductId, startDate, endDate,
                        stockLocationId, stockLocationId, unitId, unitId)
                .fetch();

        for (StockMoveLine stockMoveLine : stockMoveLines) {
            Product product = stockMoveLine.getProduct();
            BigDecimal totalQty = stockMoveLine.getRealQty();

            boolean found = false;
            for (DashboardAliments dashboardAliments : alimentsDashboardList) {
                if (dashboardAliments.getProduct().equals(product)) {
                    dashboardAliments.setStockQty(dashboardAliments.getStockQty().add(totalQty));
                    found = true;
                    break;
                }
            }

            if (!found) {
                DashboardAliments dashboardAliments = new DashboardAliments();
                dashboardAliments.setProduct(product);
                dashboardAliments.setProductCode(product.getCode());
                dashboardAliments.setProductName(product.getName());
                dashboardAliments.setProductUnit(product.getUnit());
                dashboardAliments.setStockQty(totalQty);
                alimentsDashboardList.add(dashboardAliments);
            }
        }
        response.setValue("alimentsStockList", alimentsDashboardList);
    }

    @Transactional
    public void getLinesConsumption(ActionRequest request, ActionResponse response) throws AxelorException {
        List<DashboardAliments> consumptionDashboardList = new ArrayList<>();
        BigDecimal totalQte = BigDecimal.ZERO;
        String startDate = request.getContext().get("sixthStartDate").toString();
        String endDate = request.getContext().get("sixthEndDate").toString();

        Map<String, Object> productCategoryMap = (Map<String, Object>) request.getContext().get("sixthProductCategory");
        if(productCategoryMap == null ) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the product category !")
            );
        }

        CompanyDepartment unitMap = (CompanyDepartment) request.getContext().get("sixthUnit");
        if(unitMap == null ){
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the unit !")
            );
        }

        Map<String, Object> centerMap = (Map<String, Object>) request.getContext().get("sixthCenter");
        if(centerMap == null) {
            throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    I18n.get("Please specify the center !")
            );
        }

        Integer centerId = (Integer) centerMap.get("id");
        Integer categoryProductId = (Integer) productCategoryMap.get("id");
        Long unitId = unitMap.getId();

        List<ProductConsumptionLine> consumptionLines = productConsumptionLineRepository.all()
                .filter("self.productConsumption.status = 1" +
                        "AND self.product.productCategory.id = ? " +
                        "AND self.productConsumption.center.id = ? " +
                        "AND ( self.productConsumption.center.unit.id = ? OR self.productConsumption.hatchery.unit.id = ? ) " +
                        "AND self.productConsumption.dateTime BETWEEN ? and ?",
                        categoryProductId, centerId, unitId, unitId, startDate, endDate)
                .fetch();

        for (ProductConsumptionLine productConsumptionLine : consumptionLines) {
            Product product = productConsumptionLine.getProduct();
            BigDecimal totalQty = productConsumptionLine.getQuantityProduct();
            BigDecimal saleAverageUnitPrice = productConsumptionLine.getProduct().getCostPrice();

            boolean found = false;
            for (DashboardAliments dashboardAliments : consumptionDashboardList) {
                if (dashboardAliments.getProduct().equals(product)) {
                    totalQte = dashboardAliments.getStockQty().add(totalQty);
                    dashboardAliments.setStockQty(totalQte);
                    dashboardAliments.setSaleAmount(saleAverageUnitPrice.multiply(totalQte));
                    found = true;
                    break;
                }
            }

            if (!found) {
                DashboardAliments dashboardAliments = new DashboardAliments();
                dashboardAliments.setProduct(product);
                dashboardAliments.setProductCode(product.getCode());
                dashboardAliments.setProductName(product.getName());
                dashboardAliments.setProductUnit(product.getUnit());
                dashboardAliments.setSaleAverageUnitPrice(saleAverageUnitPrice);
                dashboardAliments.setSaleAmount(saleAverageUnitPrice.multiply(totalQty));
                dashboardAliments.setStockQty(totalQty);
                consumptionDashboardList.add(dashboardAliments);
            }
        }
        response.setValue("productConsumptionList", consumptionDashboardList);
    }

}

