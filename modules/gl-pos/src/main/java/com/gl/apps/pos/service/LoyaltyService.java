package com.gl.apps.pos.service;

import com.gl.apps.pos.db.LoyaltyCustomer;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Loyalty Program
 * Handles loyalty points, tiers, and rewards
 */
public interface LoyaltyService {

  /**
   * Enroll customer in loyalty program
   */
  LoyaltyCustomer enrollCustomer(Long customerId, String email, String phone);

  /**
   * Calculate loyalty points for order
   */
  BigDecimal calculatePoints(SaleOrder order);

  /**
   * Add points to customer
   */
  void addPoints(LoyaltyCustomer customer, BigDecimal points);

  /**
   * Redeem points from customer
   */
  void redeemPoints(LoyaltyCustomer customer, BigDecimal points);

  /**
   * Update customer tier based on spending
   */
  void updateCustomerTier(LoyaltyCustomer customer);

  /**
   * Check if points are expiring soon
   */
  boolean arePointsExpiring(LoyaltyCustomer customer);

  /**
   * Apply loyalty discount to order
   */
  void applyLoyaltyDiscount(SaleOrder order, Long loyaltyCustomerId);

  /**
   * Get available rewards for customer
   */
  List<String> getAvailableRewards(LoyaltyCustomer customer);

  /**
   * Get loyalty customer by number
   */
  LoyaltyCustomer getLoyaltyCustomerByNumber(String loyaltyNumber);

  /**
   * Generate loyalty report
   */
  String generateLoyaltyReport(Long customerId);
}

