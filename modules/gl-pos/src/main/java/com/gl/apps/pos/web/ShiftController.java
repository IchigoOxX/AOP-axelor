package com.gl.apps.pos.web;


import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.inject.Beans;
import com.axelor.pos.db.Shift;
import com.axelor.pos.db.repo.ShiftRepository;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;

import com.gl.apps.pos.service.ShiftService;
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
@Path("/shifts")
public class ShiftController {
    @Inject protected ShiftService shiftService;

    @POST
    @Path("updateAfterSale")
    public Response updateShiftAfterSaleOrder(Request request) {
        Response response = new Response();
        try {
            Map<String, Object> requestData = request.getData();
            long shiftId = ((Number) requestData.get("shiftId")).longValue();
            long saleOrderId = ((Number) requestData.get("saleOrderId")).longValue();
            Shift shift = shiftService.updateShiftAfterSale(shiftId, saleOrderId);
            response.setData(shift);
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

