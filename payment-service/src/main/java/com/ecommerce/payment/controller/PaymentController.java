package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Payment API", description = "APIs for processing payments")
public class PaymentController {

    @Inject
    PaymentService paymentService;

    @POST
    @Operation(summary = "Process a new payment")
    public Response processPayment(@Valid PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/order/{orderId}")
    @Operation(summary = "Get payment details by order ID")
    public PaymentResponse getPaymentByOrderId(@PathParam("orderId") UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }
}
