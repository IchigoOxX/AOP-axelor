package com.gl.apps.pos.service;

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

public interface AnalyticsService {
	public Map<String, Object> generateDailySalesReport(Long terminalId, String date);
	public Map<String, Object> generateMonthlySalesReport(Long terminalId, String month, String year);
	public Map<String, Object> getPaymentMethodBreakdown(Long terminalId, String startDate, String endDate);
	public List<Map<String, Object>> getTopSellingItems(Long terminalId, int limit, String period);
	public List<Map<String, Object>> getBottomPerformingItems(Long terminalId, int limit, String period);
	public Map<Integer, Map<String, Object>> getHourlySalesDistribution(Long terminalId, String date);
	public Map<String, Object> getCustomerStatistics(Long terminalId, String startDate, String endDate);
	public Map<String, Object> getDiscountAnalysis(Long terminalId, String startDate, String endDate);
	public Map<String, Object> getTaxSummary(Long terminalId, String startDate, String endDate);
	public String exportReportPdf(String reportType, Map<String, Object> data);
	public String exportReportExcel(String reportType, Map<String, Object> data);
	public Map<String, Object> getKpiMetrics(Long terminalId, String period);
}
