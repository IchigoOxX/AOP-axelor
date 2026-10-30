package com.gl.apps.pos.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.base.db.Product;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Barcode operations
 * Handles barcode scanning and product lookup
 */
public interface BarcodeService {

  /**
   * Scan barcode and return product
   */
  Product scanBarcode(String barcode);

  /**
   * Get product by barcode
   */
  Product getProductByBarcode(String barcode);

  /**
   * Validate barcode format
   */
  boolean isValidBarcode(String barcode);

  /**
   * Search products by barcode pattern
   */
  List<Product> searchByBarcodePattern(String pattern);

  /**
   * Add barcode to product
   */
  void addBarcodeToProduct(Long productId, String barcode);

  /**
   * Add line item to order by barcode
   */
  void addLineItemByBarcode(SaleOrder order, String barcode, java.math.BigDecimal quantity);

  /**
   * Get barcode configuration
   */
  Map<String, Object> getBarcodeConfig();

  /**
   * Validate product available in stock
   */
  boolean isProductAvailable(Long productId, java.math.BigDecimal quantity);
}

