package com.ecommerce.order.controller;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for OrderTrackingController.
 */
@QuarkusTest
class OrderTrackingControllerContractTest {

    @Test
    @DisplayName("Get order tracking - contract test")
    void getOrderTracking_contract() {
        UUID orderId = UUID.randomUUID();

        given()
        .when()
                .get("/api/orders/" + orderId + "/tracking")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode())
                .body("message", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Get order timeline - contract test")
    void getOrderTimeline_contract() {
        UUID orderId = UUID.randomUUID();

        given()
        .when()
                .get("/api/orders/" + orderId + "/timeline")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }
}

