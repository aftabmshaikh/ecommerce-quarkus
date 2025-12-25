package com.ecommerce.cart.controller;

import com.ecommerce.cart.testsupport.PostgresTestResource;
import com.ecommerce.cart.dto.CartItemRequest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for {@link CartController}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class CartControllerContractTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Test
    @DisplayName("Get cart returns 200 and cart structure")
    void getCart_contract() {
        given()
                .header("X-User-Id", UUID.randomUUID())
                .accept(ContentType.JSON)
        .when()
                .get("/api/cart")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("userId", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Add item to cart - happy path contract")
    void addItem_contract() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(UUID.randomUUID());
        request.setQuantity(1);

        given()
                .header("X-User-Id", UUID.randomUUID())
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/cart/items")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("items", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Update cart item - happy path contract")
    void updateCartItem_contract() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        given()
                .header("X-User-Id", userId)
                .queryParam("quantity", 3)
        .when()
                .put("/api/cart/items/" + itemId)
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.NOT_FOUND.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Remove item from cart - happy path contract")
    void removeItemFromCart_contract() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        given()
                .header("X-User-Id", userId)
        .when()
                .delete("/api/cart/items/" + itemId)
        .then()
                .statusCode(Matchers.anyOf(
                        Matchers.is(Response.Status.OK.getStatusCode()),
                        Matchers.is(Response.Status.NOT_FOUND.getStatusCode())
                ));
    }

    @Test
    @DisplayName("Clear cart - happy path contract")
    void clearCart_contract() {
        given()
                .header("X-User-Id", UUID.randomUUID())
        .when()
                .delete("/api/cart")
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}


