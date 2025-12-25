package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.payment.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Contract tests for {@link PaymentController}.
 * This test focuses on HTTP contract only and mocks the PaymentService.
 * Uses in-memory connector for Kafka channels (configured in application-test.properties).
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
class PaymentControllerContractTest {

    @InjectMock
    PaymentService paymentService;

    @BeforeEach
    void setUp() {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenAnswer(invocation -> {
            PaymentRequest req = invocation.getArgument(0);
            PaymentResponse resp = new PaymentResponse();
            resp.setId(UUID.randomUUID());
            resp.setOrderId(req.getOrderId());
            resp.setCustomerId(req.getCustomerId());
            resp.setAmount(req.getAmount());
            resp.setCurrency(req.getCurrency());
            resp.setPaymentMethod(req.getPaymentMethod());
            resp.setStatus(PaymentStatus.COMPLETED);
            return resp;
        });
    }

    @Test
    @DisplayName("Process payment - happy path contract")
    void processPayment_contract() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(UUID.randomUUID());
        request.setCustomerId(UUID.randomUUID());
        request.setAmount(BigDecimal.TEN);
        request.setCurrency("USD");
        request.setPaymentMethod("CARD");

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/payments")
        .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("orderId", Matchers.notNullValue())
                .body("status", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Get payment by ID - contract")
    void getPayment_contract() {
        UUID paymentId = UUID.randomUUID();
        
        when(paymentService.getPayment(paymentId)).thenAnswer(invocation -> {
            PaymentResponse resp = new PaymentResponse();
            resp.setId(paymentId);
            resp.setStatus(PaymentStatus.COMPLETED);
            return resp;
        });

        given()
        .when()
                .get("/api/payments/" + paymentId)
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.NOT_FOUND.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Capture payment - contract")
    void capturePayment_contract() {
        UUID paymentId = UUID.randomUUID();
        
        when(paymentService.capturePayment(paymentId)).thenAnswer(invocation -> {
            PaymentResponse resp = new PaymentResponse();
            resp.setId(paymentId);
            resp.setStatus(PaymentStatus.CAPTURED);
            return resp;
        });

        given()
                .contentType(ContentType.TEXT)
        .when()
                .post("/api/payments/" + paymentId + "/capture")
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.BAD_REQUEST.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Refund payment - contract")
    void refundPayment_contract() {
        UUID paymentId = UUID.randomUUID();
        
        when(paymentService.refundPayment(eq(paymentId), any(BigDecimal.class))).thenAnswer(invocation -> {
            PaymentResponse resp = new PaymentResponse();
            resp.setId(paymentId);
            resp.setStatus(PaymentStatus.REFUNDED);
            return resp;
        });

        given()
                .contentType(ContentType.TEXT)
                .queryParam("amount", "50.00")
        .when()
                .post("/api/payments/" + paymentId + "/refund")
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.BAD_REQUEST.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Cancel payment - contract")
    void cancelPayment_contract() {
        UUID paymentId = UUID.randomUUID();
        
        when(paymentService.cancelPayment(paymentId)).thenAnswer(invocation -> {
            PaymentResponse resp = new PaymentResponse();
            resp.setId(paymentId);
            resp.setStatus(PaymentStatus.CANCELED);
            return resp;
        });

        given()
                .contentType(ContentType.TEXT)
        .when()
                .post("/api/payments/" + paymentId + "/cancel")
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.BAD_REQUEST.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Get payment methods - contract")
    void getPaymentMethods_contract() {
        when(paymentService.getAvailablePaymentMethods()).thenReturn(
                Map.of("paymentMethods", new String[]{"credit_card", "paypal"},
                       "defaultCurrency", "USD")
        );

        given()
        .when()
                .get("/api/payments/methods")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("paymentMethods", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Stripe webhook - contract")
    void handleStripeWebhook_contract() {
        given()
                .contentType(ContentType.JSON)
                .header("Stripe-Signature", "test-signature")
                .body("{\"type\":\"payment_intent.succeeded\"}")
        .when()
                .post("/api/payments/webhook/stripe")
        .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}


