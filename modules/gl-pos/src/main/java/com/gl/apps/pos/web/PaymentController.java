package com.gl.apps.pos.web;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.repo.PaymentSessionAccountRepository;
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/payment")
public class PaymentController {

    @Inject protected PaymentSessionAccountRepository paymentSessionAccountRepository;
    @POST
    public Response payForSaleOrder(Request request) {
        Response response = new Response();

        try {
            Map<String, Object> requestData = request.getData();

//            PaymentSession paymentSession = paymentSessionAccountRepository.save()

//            response.setData();
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

