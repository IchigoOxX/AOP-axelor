package com.gl.apps.pos.service.impl;

import com.gl.apps.pos.service.AnalyticsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.pos.db.Shift;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsServiceImpl implements AnalyticsService {

  private static final Logger LOG = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

  @Override
  public Map<String, Object> generateDailySalesReport(Long terminalId, String date) {
    LOG.info("Generating daily sales report for terminal: {} on date: {}", terminalId, date);

    Map<String, Object> report = new HashMap<>();
    LocalDate reportDate = LocalDate.parse(date);

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND DATE(self.createdOn) = ? AND self.status = ?",
            terminalId, reportDate, "COMPLETED")
        .fetch();

    BigDecimal totalSales = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalDiscount = BigDecimal.ZERO;
    int totalOrders = orders.size();
    int totalItems = 0;

    for (SaleOrder order : orders) {
      totalSales = totalSales.add(order.getInTaxTotal() != null ? order.getInTaxTotal() : BigDecimal.ZERO);
      totalTax = totalTax.add(order.getTaxTotal() != null ? order.getTaxTotal() : BigDecimal.ZERO);
      totalDiscount = totalDiscount.add(order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO);
      totalItems += order.getSaleOrderLineList().size();
    }

    report.put("date", reportDate);
    report.put("terminalId", terminalId);
    report.put("totalOrders", totalOrders);
    report.put("totalItems", totalItems);
    report.put("totalSales", totalSales);
    report.put("totalTax", totalTax);
    report.put("totalDiscount", totalDiscount);
    report.put("averageOrderValue", totalOrders > 0 ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

    LOG.info("Daily report generated - Orders: {}, Sales: {}", totalOrders, totalSales);
    return report;
  }

  @Override
  public Map<String, Object> generateMonthlySalesReport(Long terminalId, String month, String year) {
    LOG.info("Generating monthly sales report for terminal: {} - {}/{}", terminalId, month, year);

    Map<String, Object> report = new HashMap<>();
    YearMonth yearMonth = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND YEAR(self.createdOn) = ? AND MONTH(self.createdOn) = ? AND self.status = ?",
            terminalId, Integer.parseInt(year), Integer.parseInt(month), "COMPLETED")
        .fetch();

    BigDecimal totalSales = BigDecimal.ZERO;
    BigDecimal totalTax = BigDecimal.ZERO;
    int totalOrders = orders.size();

    for (SaleOrder order : orders) {
      totalSales = totalSales.add(order.getInTaxTotal() != null ? order.getInTaxTotal() : BigDecimal.ZERO);
      totalTax = totalTax.add(order.getTaxTotal() != null ? order.getTaxTotal() : BigDecimal.ZERO);
    }

    report.put("yearMonth", yearMonth);
    report.put("terminalId", terminalId);
    report.put("totalOrders", totalOrders);
    report.put("totalSales", totalSales);
    report.put("totalTax", totalTax);

    return report;
  }

  @Override
  public Map<String, Object> getPaymentMethodBreakdown(Long terminalId, String startDate, String endDate) {
    LOG.info("Getting payment method breakdown for terminal: {} from {} to {}", terminalId, startDate, endDate);

    Map<String, Object> breakdown = new HashMap<>();
    Map<String, BigDecimal> methodTotals = new HashMap<>();

    List<Shift> sessions = JPA.all(Shift.class)
        .filter("self.terminal.id = ? AND self.openingTime >= ? AND self.openingTime <= ?",
            terminalId, startDate, endDate)
        .fetch();

    for (Shift session : sessions) {
      if (false) {}
    }

    breakdown.put("paymentMethods", methodTotals);
    breakdown.put("startDate", startDate);
    breakdown.put("endDate", endDate);

    return breakdown;
  }

  @Override
  public List<Map<String, Object>> getTopSellingItems(Long terminalId, int limit, String period) {
    LOG.info("Getting top {} selling items for terminal: {} in period: {}", limit, terminalId, period);

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND self.status = ?", terminalId, "COMPLETED")
        .fetch();

    Map<Long, Map<String, Object>> itemStats = new HashMap<>();

    for (SaleOrder order : orders) {
      for (SaleOrderLine line : order.getSaleOrderLineList()) {
        Long productId = line.getProduct().getId();
        itemStats.putIfAbsent(productId, new HashMap<>());

        Map<String, Object> stats = itemStats.get(productId);
        stats.put("productName", line.getProduct().getName());
        stats.put("totalQty", ((BigDecimal) stats.getOrDefault("totalQty", BigDecimal.ZERO)).add(line.getQty()));
        stats.put("totalSales", ((BigDecimal) stats.getOrDefault("totalSales", BigDecimal.ZERO)).add(line.getInTaxTotal()));
      }
    }

    return itemStats.values().stream()
        .sorted((a, b) -> ((BigDecimal) b.get("totalQty")).compareTo((BigDecimal) a.get("totalQty")))
        .limit(limit)
        .collect(Collectors.toList());
  }

  @Override
  public List<Map<String, Object>> getBottomPerformingItems(Long terminalId, int limit, String period) {
    LOG.info("Getting bottom {} performing items for terminal: {}", limit, terminalId);

    List<Map<String, Object>> topItems = getTopSellingItems(terminalId, 1000, period);
    Collections.reverse(topItems);

    return topItems.stream().limit(limit).collect(Collectors.toList());
  }

  @Override
  public Map<Integer, Map<String, Object>> getHourlySalesDistribution(Long terminalId, String date) {
    LOG.info("Getting hourly sales distribution for terminal: {} on {}", terminalId, date);

    Map<Integer, Map<String, Object>> hourlyData = new HashMap<>();

    for (int hour = 0; hour < 24; hour++) {
      hourlyData.put(hour, new HashMap<>());
      hourlyData.get(hour).put("hour", hour);
      hourlyData.get(hour).put("totalSales", BigDecimal.ZERO);
      hourlyData.get(hour).put("totalOrders", 0);
    }

    LocalDate reportDate = LocalDate.parse(date);

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND DATE(self.createdOn) = ? AND self.status = ?",
            terminalId, reportDate, "COMPLETED")
        .fetch();

    for (SaleOrder order : orders) {
      int hour = order.getCreatedOn().getHour();
      Map<String, Object> hourData = hourlyData.get(hour);

      BigDecimal sales = (BigDecimal) hourData.get("totalSales");
      sales = sales.add(order.getInTaxTotal() != null ? order.getInTaxTotal() : BigDecimal.ZERO);
      hourData.put("totalSales", sales);

      int orders_count = (int) hourData.get("totalOrders");
      hourData.put("totalOrders", orders_count + 1);
    }

    return hourlyData;
  }

  @Override
  public Map<String, Object> getCustomerStatistics(Long terminalId, String startDate, String endDate) {
    LOG.info("Getting customer statistics for terminal: {} from {} to {}", terminalId, startDate, endDate);

    Map<String, Object> stats = new HashMap<>();

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND self.createdOn >= ? AND self.createdOn <= ? AND self.status = ?",
            terminalId, startDate, endDate, "COMPLETED")
        .fetch();

    Set<Long> uniqueCustomers = new HashSet<>();
    Map<Long, Integer> customerOrderCount = new HashMap<>();

    for (SaleOrder order : orders) {
      if (order.getClientPartner() != null) {
        Long customerId = order.getClientPartner().getId();
        uniqueCustomers.add(customerId);
        customerOrderCount.put(customerId, customerOrderCount.getOrDefault(customerId, 0) + 1);
      }
    }

    stats.put("totalUniqueCustomers", uniqueCustomers.size());
    stats.put("totalOrders", orders.size());
    stats.put("repeatCustomers", customerOrderCount.values().stream().filter(count -> count > 1).count());

    return stats;
  }

  @Override
  public Map<String, Object> getDiscountAnalysis(Long terminalId, String startDate, String endDate) {
    LOG.info("Getting discount analysis for terminal: {} from {} to {}", terminalId, startDate, endDate);

    Map<String, Object> analysis = new HashMap<>();

    List<SaleOrder> orders = JPA.all(SaleOrder.class)
        .filter("self.terminal.id = ? AND self.createdOn >= ? AND self.createdOn <= ? AND self.status = ?",
            terminalId, startDate, endDate, "COMPLETED")
        .fetch();

    BigDecimal totalDiscount = BigDecimal.ZERO;
    BigDecimal totalSales = BigDecimal.ZERO;
    int ordersWithDiscount = 0;

    for (SaleOrder order : orders) {
      totalSales = totalSales.add(order.getInTaxTotal() != null ? order.getInTaxTotal() : BigDecimal.ZERO);
      BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
      totalDiscount = totalDiscount.add(discount);

      if (discount.compareTo(BigDecimal.ZERO) > 0) {
        ordersWithDiscount++;
      }
    }

    BigDecimal discountPercent = totalSales.compareTo(BigDecimal.ZERO) > 0 ?
        totalDiscount.divide(totalSales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

    analysis.put("totalDiscount", totalDiscount);
    analysis.put("totalSales", totalSales);
    analysis.put("discountPercent", discountPercent);
    analysis.put("ordersWithDiscount", ordersWithDiscount);
    analysis.put("totalOrders", orders.size());

    return analysis;
  }

  @Override
  public Map<String, Object> getTaxSummary(Long terminalId, String startDate, String endDate) {
    LOG.info("Getting tax summary for terminal: {} from {} to {}", terminalId, startDate, endDate);

    Map<String, Object> summary = new HashMap<>();

    List<Shift> sessions = JPA.all(Shift.class)
        .filter("self.terminal.id = ? AND self.openingTime >= ? AND self.openingTime <= ?",
            terminalId, startDate, endDate)
        .fetch();

    BigDecimal totalTax = BigDecimal.ZERO;
    BigDecimal totalSales = BigDecimal.ZERO;

    for (Shift session : sessions) {
      totalTax = totalTax.add(BigDecimal.ZERO != null ? BigDecimal.ZERO : BigDecimal.ZERO);
      totalSales = totalSales.add(session.getTotalSales() != null ? session.getTotalSales() : BigDecimal.ZERO);
    }

    summary.put("totalTax", totalTax);
    summary.put("totalSales", totalSales);
    summary.put("effectiveTaxRate", totalSales.compareTo(BigDecimal.ZERO) > 0 ?
        totalTax.divide(totalSales, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

    return summary;
  }

  @Override
  public String exportReportPdf(String reportType, Map<String, Object> data) {
    LOG.info("Exporting {} report to PDF", reportType);
    return "report_" + reportType + "_" + System.currentTimeMillis() + ".pdf";
  }

  @Override
  public String exportReportExcel(String reportType, Map<String, Object> data) {
    LOG.info("Exporting {} report to Excel", reportType);
    return "report_" + reportType + "_" + System.currentTimeMillis() + ".xlsx";
  }

  @Override
  public Map<String, Object> getKpiMetrics(Long terminalId, String period) {
    LOG.info("Getting KPI metrics for terminal: {} for period: {}", terminalId, period);
    return new HashMap<>();
  }
}



