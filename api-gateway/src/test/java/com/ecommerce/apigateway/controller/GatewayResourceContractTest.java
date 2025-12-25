package com.ecommerce.apigateway.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Basic contract tests for {@link GatewayResource}.
 */
@QuarkusTest
class GatewayResourceContractTest {

    @Test
    @DisplayName("Unknown path returns 404 from gateway")
    void unknownPath_returns404() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/non-existing-path")
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}


