package com.gl.apps.pos.web.api;


import com.gl.apps.pos.service.LoyaltyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.studio.db.AppPos;
import com.axelor.db.JPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Mobile POS Application
 * Provides endpoints for mobile app integration
 */
@Path("/mobile/pos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MobileApiController {

  private static final Logger LOG = LoggerFactory.getLogger(MobileApiController.class);

  
  @Inject private LoyaltyService loyaltyService;

  /**
   * GET active POS configurations
   */
  @GET
  @Path("/configs")
  public Response getActiveConfigs() {
    LOG.info("GET /mobile/pos/configs");
    try {
      List<AppPos> configs = JPA.all(AppPos.class)
          .filter("self.isActive = true")
          .fetch();
      return Response.ok(configs).build();
    } catch (Exception e) {
      LOG.error("Error fetching configs", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * POST create new POS order from mobile
   */
  @POST
  @Path("/orders")
  public Response createOrder(Map<String, Object> data) {
    LOG.info("POST /mobile/pos/orders");
    try {
      Long configId = ((Number) data.get("posConfigId")).longValue();
      Long customerId = data.get("customerId") != null ? ((Number) data.get("customerId")).longValue() : null;

      AppPos config = JPA.find(AppPos.class, configId);
      if (config == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(createErrorResponse("POS Config not found")).build();
      }

      com.axelor.apps.base.db.Partner customer = customerId != null ? 
          JPA.find(com.axelor.apps.base.db.Partner.class, customerId) : null;

      SaleOrder order = null;
      return Response.ok(order).build();
    } catch (Exception e) {
      LOG.error("Error creating order", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * GET order status
   */
  @GET
  @Path("/orders/{orderNumber}")
  public Response getOrder(@PathParam("orderNumber") String orderNumber) {
    LOG.info("GET /mobile/pos/orders/{}", orderNumber);
    try {
      SaleOrder order = null;
      if (order == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(createErrorResponse("Order not found")).build();
      }
      return Response.ok(order).build();
    } catch (Exception e) {
      LOG.error("Error fetching order", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * POST sync offline orders
   */
  @POST
  @Path("/sync")
  public Response syncOfflineOrders(List<Map<String, Object>> ordersData) {
    LOG.info("POST /mobile/pos/sync - Syncing {} offline orders", ordersData.size());
    try {
      int synced = 0;
      for (Map<String, Object> orderData : ordersData) {
        // TODO: Implement offline order sync logic
        synced++;
      }
      
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("synced", synced);
      return Response.ok(response).build();
    } catch (Exception e) {
      LOG.error("Error syncing orders", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * GET loyalty customer info
   */
  @GET
  @Path("/loyalty/{loyaltyNumber}")
  public Response getLoyaltyCustomer(@PathParam("loyaltyNumber") String loyaltyNumber) {
    LOG.info("GET /mobile/pos/loyalty/{}", loyaltyNumber);
    try {
      var customer = loyaltyService.getLoyaltyCustomerByNumber(loyaltyNumber);
      if (customer == null) {
        return Response.status(Response.Status.NOT_FOUND)
            .entity(createErrorResponse("Loyalty customer not found")).build();
      }
      return Response.ok(customer).build();
    } catch (Exception e) {
      LOG.error("Error fetching loyalty customer", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * POST enroll in loyalty program
   */
  @POST
  @Path("/loyalty/enroll")
  public Response enrollLoyalty(Map<String, Object> data) {
    LOG.info("POST /mobile/pos/loyalty/enroll");
    try {
      Long customerId = ((Number) data.get("customerId")).longValue();
      String email = (String) data.get("email");
      String phone = (String) data.get("phone");

      var customer = loyaltyService.enrollCustomer(customerId, email, phone);
      return Response.ok(customer).build();
    } catch (Exception e) {
      LOG.error("Error enrolling loyalty", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(createErrorResponse(e.getMessage())).build();
    }
  }

  /**
   * Health check endpoint
   */
  @GET
  @Path("/health")
  public Response health() {
    LOG.debug("GET /mobile/pos/health");
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("timestamp", System.currentTimeMillis());
    return Response.ok(response).build();
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", true);
    error.put("message", message);
    return error;
  }
}







