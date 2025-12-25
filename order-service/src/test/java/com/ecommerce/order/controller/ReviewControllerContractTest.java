package com.ecommerce.order.controller;

import com.ecommerce.order.dto.reviews.ReviewRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Contract tests for ReviewController.
 */
@QuarkusTest
class ReviewControllerContractTest {

    @Test
    @DisplayName("Submit review - contract test")
    void submitReview_contract() {
        UUID orderId = UUID.randomUUID();
        ReviewRequest request = new ReviewRequest();
        request.setOrderId(orderId);
        request.setOrderItemId(UUID.randomUUID());
        request.setProductId(UUID.randomUUID());
        request.setTitle("Great product");
        request.setComment("Very satisfied with the purchase");
        request.setRating(5);

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/orders/" + orderId + "/reviews")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode())
                .body("message", Matchers.notNullValue());
    }

    @Test
    @DisplayName("Get order reviews - contract test")
    void getOrderReviews_contract() {
        UUID orderId = UUID.randomUUID();

        given()
        .when()
                .get("/api/orders/" + orderId + "/reviews")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    @DisplayName("Get product reviews - contract test")
    void getProductReviews_contract() {
        UUID productId = UUID.randomUUID();

        given()
                .queryParam("page", "0")
                .queryParam("size", "10")
        .when()
                .get("/api/products/" + productId + "/reviews")
        .then()
                .statusCode(Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }
}

