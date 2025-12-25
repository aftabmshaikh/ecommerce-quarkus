package com.ecommerce.order.controller;

import com.ecommerce.order.dto.returns.ReturnRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for OrderReturnController.
 * These are basic contract tests since the controller returns NOT_IMPLEMENTED.
 */
@QuarkusTest
class OrderReturnControllerContractTest {

    @Test
    @DisplayName("Initiate return - contract test")
    void initiateReturn_contract() {
        UUID orderId = UUID.randomUUID();
        ReturnRequest request = new ReturnRequest();
        request.setReason("Defective item");
        request.setComments("Item arrived damaged");
        request.setItems(new ArrayList<>());

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/orders/" + orderId + "/return")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode())
                .body("message", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Get return history - contract test")
    void getReturnHistory_contract() {
        given()
        .when()
                .get("/api/orders/returns")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    @DisplayName("Get return details - contract test")
    void getReturnDetails_contract() {
        UUID returnId = UUID.randomUUID();

        given()
        .when()
                .get("/api/orders/returns/" + returnId)
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }
}

