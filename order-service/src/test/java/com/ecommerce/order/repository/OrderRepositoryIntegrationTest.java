package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DB-level tests for {@link OrderRepository}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class OrderRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    OrderRepository orderRepository;

    @Test
    @DisplayName("findByOrderNumber returns persisted order")
    @Transactional
    void findByOrderNumber_returnsPersistedOrder() {
        Order order = new Order();
        order.setOrderNumber("ORD-INT-1");
        order.setCustomerId(UUID.randomUUID());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Test St");
        order.setBillingAddress("123 Test St");
        order.setCustomerEmail("test@example.com");
        order.setCustomerPhone("1234567890");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.persist(order);

        Order found = orderRepository.findByOrderNumber("ORD-INT-1")
                .orElseThrow(() -> new AssertionError("Order not found by orderNumber"));

        assertThat(found.getOrderNumber()).isEqualTo("ORD-INT-1");
    }
}


