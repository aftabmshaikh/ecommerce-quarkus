package com.ecommerce.notification.controller;

import com.ecommerce.notification.testsupport.PostgresTestResource;
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
 * Contract tests for {@link NotificationController}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class NotificationControllerContractTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Test
    @DisplayName("Pending notification count returns JSON with count field")
    void pendingCount_contract() {
        given()
                .accept(ContentType.JSON)
                .queryParam("email", "test@example.com")
        .when()
                .get("/api/notifications/pending/count")
        .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("count", Matchers.notNullValue());
    }
}


