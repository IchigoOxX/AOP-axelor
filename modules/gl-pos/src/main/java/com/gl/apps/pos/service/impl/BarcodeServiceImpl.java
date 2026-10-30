package com.gl.apps.pos.service.impl;

import com.gl.apps.pos.service.BarcodeService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.base.db.Product;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarcodeServiceImpl implements BarcodeService {

  private static final Logger LOG = LoggerFactory.getLogger(BarcodeServiceImpl.class);

  @Override
  public Product scanBarcode(String barcode) {
    LOG.info("Scanning barcode: {}", barcode);
    if (!isValidBarcode(barcode)) throw new IllegalArgumentException("Invalid barcode");
    Product product = getProductByBarcode(barcode);
    if (product == null) throw new RuntimeException("Product not found");
    return product;
  }

  @Override
  public Product getProductByBarcode(String barcode) {
    return JPA.all(Product.class).filter("self.barCode = ? OR self.code = ?", barcode, barcode).fetchOne();
  }

  @Override
  public boolean isValidBarcode(String barcode) {
    if (barcode == null || barcode.trim().isEmpty()) return false;
    String clean = barcode.replaceAll("\\s+", "");
    return clean.length() >= 8 && clean.length() <= 18;
  }

  @Override
  public List<Product> searchByBarcodePattern(String pattern) {
    return JPA.all(Product.class).filter("self.barCode LIKE ? OR self.code LIKE ?", "%" + pattern + "%", "%" + pattern + "%").fetch();
  }

  @Override
  public void addBarcodeToProduct(Long productId, String barcode) {
    Product product = JPA.find(Product.class, productId);
    if (product == null) throw new IllegalArgumentException("Product not found");
    if (!isValidBarcode(barcode)) throw new IllegalArgumentException("Invalid barcode");
    product.setCode(barcode);
    JPA.save(product);
  }

  @Override
  public void addLineItemByBarcode(SaleOrder order, String barcode, BigDecimal quantity) {
    Product product = scanBarcode(barcode);
    if (!isProductAvailable(product.getId(), quantity)) throw new RuntimeException("Insufficient stock");
  }

  @Override
  public Map<String, Object> getBarcodeConfig() {
    Map<String, Object> config = new HashMap<>();
    config.put("minLength", 8);
    config.put("maxLength", 18);
    config.put("format", "EAN13");
    config.put("enableScanning", true);
    return config;
  }

  @Override
  public boolean isProductAvailable(Long productId, BigDecimal quantity) {
    Product product = JPA.find(Product.class, productId);
    return product != null;
  }
}


