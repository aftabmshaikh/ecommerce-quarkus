package com.ecommerce.user.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for {@link UserController}.
 * Uses Quarkus TestSecurity to simulate an authenticated user instead of starting real Keycloak.
 */
@QuarkusTest
class UserControllerContractTest {

    @Test
    @TestSecurity(user = "test-user", roles = {"USER"})
    @DisplayName("Get user by id - not found contract")
    void getUserById_notFound_contract() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/api/users/{id}", UUID.randomUUID())
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}


