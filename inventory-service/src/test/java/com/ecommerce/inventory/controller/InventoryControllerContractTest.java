package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.testsupport.KafkaTestResource;
import com.ecommerce.inventory.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import static io.restassured.RestAssured.given;

/**
 * Basic contract tests for {@link InventoryController} backed by Testcontainers Postgres and Kafka.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class InventoryControllerContractTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Test
    @DisplayName("Health endpoint returns expected message")
    void health_contract() {
        given()
                .accept(ContentType.TEXT)
        .when()
                .get("/api/inventory/health")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(Matchers.containsString("Inventory Service is healthy"));
    }
}


