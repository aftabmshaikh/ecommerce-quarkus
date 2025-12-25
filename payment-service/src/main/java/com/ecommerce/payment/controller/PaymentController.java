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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Path("/api/payments")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Payment API", description = "APIs for processing payments")
public class PaymentController {

    @Inject
    PaymentService paymentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Process a payment for an order")
    public Response processPayment(@Valid PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{paymentId}")
    @Operation(summary = "Get payment details by ID")
    public PaymentResponse getPayment(@PathParam("paymentId") UUID paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @POST
    @Path("/{paymentId}/capture")
    @Consumes(MediaType.WILDCARD)
    @Operation(summary = "Capture an authorized payment")
    public PaymentResponse capturePayment(@PathParam("paymentId") UUID paymentId) {
        return paymentService.capturePayment(paymentId);
    }

    @POST
    @Path("/{paymentId}/refund")
    @Consumes(MediaType.WILDCARD)
    @Operation(summary = "Refund a payment")
    public PaymentResponse refundPayment(
            @PathParam("paymentId") UUID paymentId,
            @QueryParam("amount") BigDecimal amount) {
        return paymentService.refundPayment(paymentId, amount);
    }

    @POST
    @Path("/{paymentId}/cancel")
    @Consumes(MediaType.WILDCARD)
    @Operation(summary = "Cancel a payment")
    public PaymentResponse cancelPayment(@PathParam("paymentId") UUID paymentId) {
        return paymentService.cancelPayment(paymentId);
    }

    @GET
    @Path("/methods")
    @Operation(summary = "Get available payment methods")
    public Map<String, Object> getPaymentMethods() {
        return paymentService.getAvailablePaymentMethods();
    }

    @POST
    @Path("/webhook/stripe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Stripe webhook for payment events")
    public Response handleStripeWebhook(
            String payload,
            @HeaderParam("Stripe-Signature") String sigHeader) {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return Response.ok().build();
    }
}
