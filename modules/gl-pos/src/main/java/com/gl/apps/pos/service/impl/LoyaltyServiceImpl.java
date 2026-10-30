package com.gl.apps.pos.service.impl;

import com.gl.apps.pos.service.LoyaltyService;
import com.gl.apps.pos.db.LoyaltyCustomer;
import com.gl.apps.pos.db.LoyaltyTransaction;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoyaltyServiceImpl implements LoyaltyService {

  private static final Logger LOG = LoggerFactory.getLogger(LoyaltyServiceImpl.class);
  private static final BigDecimal POINTS_PER_DOLLAR = BigDecimal.valueOf(1);

  @Override
  public LoyaltyCustomer enrollCustomer(Long customerId, String email, String phone) {
    LOG.info("Enrolling customer {} in loyalty program", customerId);
    Partner customer = JPA.find(Partner.class, customerId);
    if (customer == null) throw new IllegalArgumentException("Customer not found");

    LoyaltyCustomer loyaltyCustomer = new LoyaltyCustomer();
    loyaltyCustomer.setCustomer(customer);
    loyaltyCustomer.setLoyaltyNumber("LOY" + System.currentTimeMillis());
    loyaltyCustomer.setTier("BRONZE");
    loyaltyCustomer.setAvailablePoints(BigDecimal.ZERO);
    loyaltyCustomer.setTotalPointsEarned(BigDecimal.ZERO);
    loyaltyCustomer.setTotalPointsRedeemed(BigDecimal.ZERO);
    loyaltyCustomer.setTotalTransactions(0);
    loyaltyCustomer.setTotalSpent(BigDecimal.ZERO);
    loyaltyCustomer.setEnrollmentDate(LocalDateTime.now());
    loyaltyCustomer.setIsActive(true);
    loyaltyCustomer.setEmail(email);
    loyaltyCustomer.setPhone(phone);
    loyaltyCustomer.setCreatedOn(LocalDateTime.now());
    loyaltyCustomer.setUpdatedOn(LocalDateTime.now());
    JPA.save(loyaltyCustomer);
    return loyaltyCustomer;
  }

  @Override
  public BigDecimal calculatePoints(SaleOrder order) {
    BigDecimal points = order.getInTaxTotal() != null ? order.getInTaxTotal().multiply(POINTS_PER_DOLLAR) : BigDecimal.ZERO;
    if (null /* order.getLoyaltyCustomer() */ != null) {
      points = points.multiply(getPointsMultiplier("BRONZE"));
    }
    return points.setScale(0, RoundingMode.HALF_UP);
  }

  @Override
  public void addPoints(LoyaltyCustomer customer, BigDecimal points) {
    customer.setAvailablePoints(customer.getAvailablePoints().add(points));
    customer.setTotalPointsEarned(customer.getTotalPointsEarned().add(points));
    customer.setUpdatedOn(LocalDateTime.now());
    JPA.save(customer);
  }

  @Override
  public void redeemPoints(LoyaltyCustomer customer, BigDecimal points) {
    if (customer.getAvailablePoints().compareTo(points) < 0) {
      throw new RuntimeException("Insufficient loyalty points");
    }
    customer.setAvailablePoints(customer.getAvailablePoints().subtract(points));
    customer.setTotalPointsRedeemed(customer.getTotalPointsRedeemed().add(points));
    customer.setUpdatedOn(LocalDateTime.now());
    JPA.save(customer);
  }

  @Override
  public void updateCustomerTier(LoyaltyCustomer customer) {
    String newTier = determineTier(customer.getTotalSpent());
    if (!customer.getTier().equals(newTier)) {
      customer.setTier(newTier);
      JPA.save(customer);
    }
  }

  @Override
  public boolean arePointsExpiring(LoyaltyCustomer customer) {
    LocalDateTime lastActivity = customer.getLastTransactionDate() != null ? customer.getLastTransactionDate() : customer.getEnrollmentDate();
    return LocalDateTime.now().isAfter(lastActivity.plusYears(1).minusMonths(1));
  }

  @Override
  public void applyLoyaltyDiscount(SaleOrder order, Long loyaltyCustomerId) {
    LoyaltyCustomer customer = JPA.find(LoyaltyCustomer.class, loyaltyCustomerId);
    if (customer == null || !customer.getIsActive()) throw new RuntimeException("Invalid loyalty customer");
    BigDecimal discountAmount = order.getInTaxTotal().multiply(getDiscountPercent(customer.getTier())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    order.setDiscountAmount((order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO).add(discountAmount));
    order.setInTaxTotal(order.getInTaxTotal().subtract(discountAmount));
    JPA.save(order);
  }

  @Override
  public List<String> getAvailableRewards(LoyaltyCustomer customer) {
    List<String> rewards = new ArrayList<>();
    switch (customer.getTier()) {
      case "BRONZE": rewards.add("5% discount"); break;
      case "SILVER": rewards.add("10% discount"); rewards.add("Free shipping"); break;
      case "GOLD": rewards.add("15% discount"); rewards.add("Free shipping"); rewards.add("Early access"); break;
      case "PLATINUM": rewards.add("20% discount"); rewards.add("VIP support"); break;
    }
    return rewards;
  }

  @Override
  public LoyaltyCustomer getLoyaltyCustomerByNumber(String loyaltyNumber) {
    return JPA.all(LoyaltyCustomer.class).filter("self.loyaltyNumber = ?", loyaltyNumber).fetchOne();
  }

  @Override
  public String generateLoyaltyReport(Long customerId) {
    LoyaltyCustomer customer = JPA.find(LoyaltyCustomer.class, customerId);
    if (customer == null) return "Not found";
    return "Loyalty #: " + customer.getLoyaltyNumber() + " Tier: " + customer.getTier() + " Points: " + customer.getAvailablePoints();
  }

  private String determineTier(BigDecimal totalSpent) {
    if (totalSpent.compareTo(BigDecimal.valueOf(5000)) >= 0) return "PLATINUM";
    if (totalSpent.compareTo(BigDecimal.valueOf(2000)) >= 0) return "GOLD";
    if (totalSpent.compareTo(BigDecimal.valueOf(500)) >= 0) return "SILVER";
    return "BRONZE";
  }

  private BigDecimal getPointsMultiplier(String tier) {
    switch (tier) {
      case "SILVER": return BigDecimal.valueOf(1.25);
      case "GOLD": return BigDecimal.valueOf(1.5);
      case "PLATINUM": return BigDecimal.valueOf(2.0);
      default: return BigDecimal.ONE;
    }
  }

  private BigDecimal getDiscountPercent(String tier) {
    switch (tier) {
      case "SILVER": return BigDecimal.valueOf(10);
      case "GOLD": return BigDecimal.valueOf(15);
      case "PLATINUM": return BigDecimal.valueOf(20);
      default: return BigDecimal.valueOf(5);
    }
  }
}




