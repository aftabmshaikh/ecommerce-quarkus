package com.ecommerce.order.controller;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for ShippingController.
 */
@QuarkusTest
class ShippingControllerContractTest {

    @Test
    @DisplayName("Get shipping options - contract test")
    void getShippingOptions_contract() {
        given()
                .queryParam("country", "US")
                .queryParam("postalCode", "12345")
        .when()
                .get("/api/shipping/options")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode())
                .body("message", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Calculate shipping - contract test")
    void calculateShipping_contract() {
        given()
                .queryParam("country", "US")
                .queryParam("postalCode", "12345")
                .queryParam("weight", "2.5")
                .queryParam("value", "100.00")
        .when()
                .post("/api/shipping/calculate")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    @DisplayName("Track shipment - contract test")
    void trackShipment_contract() {
        String trackingNumber = "TRACK123456";

        given()
        .when()
                .get("/api/shipping/track/" + trackingNumber)
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }
}

