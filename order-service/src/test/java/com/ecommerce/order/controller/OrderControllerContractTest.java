package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.testsupport.KafkaTestResource;
import com.ecommerce.order.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Contract-level tests for {@link OrderController}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class OrderControllerContractTest {

    @InjectMock
    OrderService orderService;

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @BeforeEach
    void setUp() {
        // Stub OrderService.createOrder so the controller test doesn't hit real DB or product-service
        when(orderService.createOrder(any(OrderRequest.class))).thenAnswer(invocation -> {
            OrderRequest req = invocation.getArgument(0);
            OrderResponse resp = new OrderResponse();
            resp.setId(UUID.randomUUID());
            resp.setCustomerId(UUID.fromString(req.getCustomerId()));
            resp.setStatus(OrderStatus.PENDING.name());
            resp.setTotal(req.getTotal());
            return resp;
        });
    }

    @Test
    @DisplayName("Create order - happy path contract")
    void createOrder_contract() {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(UUID.randomUUID().toString());
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(10.00));
        item.setTotalPrice(BigDecimal.valueOf(10.00));

        OrderRequest request = new OrderRequest();
        request.setCustomerId(UUID.randomUUID().toString());
        request.setCustomerEmail("test@example.com");
        request.setCustomerPhone("1234567890");
        request.setShippingAddress("123 Test St");
        request.setBillingAddress("123 Test St");
        request.setSubtotal(BigDecimal.valueOf(10.00));
        request.setTax(BigDecimal.valueOf(1.00));
        request.setShippingFee(BigDecimal.valueOf(5.00));
        request.setTotal(BigDecimal.valueOf(16.00));
        request.setItems(Collections.singletonList(item));

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/orders")
        .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("id", Matchers.notNullValue())
                .body("status", Matchers.equalTo(OrderStatus.PENDING.name()));
    }

    @Test
    @DisplayName("Get order by id - not found contract")
    void getOrder_notFound_contract() {
        given()
                .accept(ContentType.JSON)
        .when()
                .get("/api/orders/{id}", UUID.randomUUID())
        .then()
                // Current implementation returns 204 No Content when an order is not found
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}

