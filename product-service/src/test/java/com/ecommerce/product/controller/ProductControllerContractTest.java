package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.testsupport.KafkaTestResource;
import com.ecommerce.product.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Advanced REST API contract tests for {@link ProductController}.
 *
 * These tests:
 * - boot the full Quarkus context
 * - use Testcontainers-backed Postgres & Kafka
 * - validate request/response contracts for key endpoints
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class ProductControllerContractTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Test
    @DisplayName("Create product - happy path contract")
    void createProduct_contract() {
        ProductRequest request = ProductRequest.builder()
                .name("Test Product")
                .description("Contract test product")
                .sku("SKU-CONTRACT-1")
                .price(BigDecimal.valueOf(19.99))
                .stockQuantity(10)
                .categoryId(null) // Category is optional
                .imageUrl("https://example.com/image.png")
                .active(true)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/products")
        .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                // Contract: response has id and echoes core fields
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo(request.getName()))
                .body("description", Matchers.equalTo(request.getDescription()))
                .body("sku", Matchers.equalTo(request.getSku()))
                .body("price", Matchers.equalTo(request.getPrice().floatValue()))
                .body("stockQuantity", Matchers.equalTo(request.getStockQuantity()))
                .body("active", Matchers.equalTo(request.getActive()));
    }

    @Test
    @DisplayName("Get product by id - not found contract")
    void getProductById_notFound_contract() {
        UUID randomId = UUID.randomUUID();

        given()
                .accept(ContentType.JSON)
        .when()
                .get("/api/products/{id}", randomId)
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}


