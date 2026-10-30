package com.gl.apps.pos.web;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.inject.Beans;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import com.axelor.studio.db.AppPos;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/saleOrder")
public class PosController {
    @Inject protected SaleOrderRepository saleOrderRepo;
    @Inject protected SaleOrderWorkflowService saleOrderWorkflowService;
    @Inject protected StockMoveService stockMoveService;
    @Inject protected StockLocationRepository stockLocationRepo;
        @POST
    @Path("confirmSaleOrder")
    public Response confirmSaleOrder(Request request) {
        Response response = new Response();

        try {
            Map<String, Object> requestData = request.getData();

            Long saleOrderId = ((Number) requestData.get("id")).longValue();

            SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);

            if (saleOrder == null) {
                throw new RuntimeException("SaleOrder not found with ID: " + saleOrderId);
            }

            saleOrderWorkflowService.completeSaleOrder(saleOrder);
            saleOrder = saleOrderRepo.find(saleOrderId);

            AppPos appPos = com.axelor.db.JPA.all(AppPos.class).fetchOne();
            StockLocation fromStockLocation =  appPos.getWarehouseLocation();
            StockLocation toStockLocation = appPos.getVirtualClientLocation();

            saleOrder.setStockLocation(fromStockLocation);
            saleOrder.setToStockLocation(toStockLocation);
            saleOrderWorkflowService.completeSaleOrder(saleOrder);


//            StockLocation fromStockLocation = stockLocationRepo.find((long) 1);
//            StockLocation toStockLocation = stockLocationRepo.find((long) 2);
//            LocalDate realDate = LocalDate.now();
//            StockMoveLine stockMoveLine =
//            stockMoveService.createStockMoveMobility(fromStockLocation, toStockLocation, saleOrder.getCompany(), null );
//            try {
//                // Finalize the quotation
//                saleOrderWorkflowService.completeSaleOrder(saleOrder);
//                System.out.println("Quotation finalized successfully");
//            } catch (Exception e) {
//                System.err.println("Error finalizing quotation: " + e);
//                response.setStatus(Response.STATUS_FAILURE);
//                response.fail("Error finalizing quotation: " + e.getMessage());
//                return response;
//            }
//
//            // Refetch the SaleOrder object after finalizing the quotation
//            saleOrder = saleOrderRepo.find(saleOrderId);
//
//            try {
//                // Confirm the sale order
//                saleOrderWorkflowService.completeSaleOrder(saleOrder);
//                System.out.println("Sale order confirmed successfully");
//            } catch (Exception e) {
//                System.err.println("Error confirming sale order: " + e.getMessage());
//                response.setStatus(Response.STATUS_FAILURE);
//                response.fail("Error confirming sale order: " + e.getMessage());
//                return response;
//            }

            response.setStatus(Response.STATUS_SUCCESS);
            response.setData(saleOrder); // Return the sale order for confirmation
        } catch (Exception e) {
            // Handle exception with logging
            e.printStackTrace();
            response.setStatus(Response.STATUS_FAILURE);
            response.addError("error",e.toString());
            response.fail(e.getMessage());
        }

        return response;
    }

    @POST
    @Path("completeSaleOrder")
    public Response completeSaleOrder(Request request) {
        Response response = new Response();

        try {
            Map<String, Object> requestData = request.getData();

            Long saleOrderId = ((Number) requestData.get("id")).longValue();
            SaleOrder saleOrder = saleOrderRepo.find(saleOrderId);

            if (saleOrder == null) {
                throw new RuntimeException("SaleOrder not found with ID: " + saleOrderId);
            }

            List<StockMove> stockMoves =
                    Beans.get(StockMoveRepository.class)
                            .all()
                            .filter("self.saleOrder.id = ?", saleOrder.getId())
                            .fetch();
            if (!stockMoves.isEmpty()) {
                for (StockMove stockMove : stockMoves) {
                    stockMoveService.realize(stockMove);
                }
            }
//            saleOrderWorkflowService.completeSaleOrder(saleOrder);

            response.setData(saleOrder);
        } catch (Exception e) {
            // Handle exception with logging
            e.printStackTrace();
            response.setStatus(Response.STATUS_FAILURE);
            response.addError("error",e.toString());
            response.fail(e.getMessage());
        }

        return response;
    }
}





