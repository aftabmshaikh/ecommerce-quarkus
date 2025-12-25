package com.ecommerce.payment.repository;

import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link PaymentRepository}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class PaymentRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    PaymentRepository paymentRepository;

    @Test
    @DisplayName("Persist payment and find by orderId")
    @Transactional
    void persistAndFindByOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(UUID.randomUUID());
        payment.setCustomerId(UUID.randomUUID());
        payment.setAmount(BigDecimal.TEN);
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        paymentRepository.persist(payment);

        Payment found = paymentRepository.find("orderId", payment.getOrderId()).firstResult();
        assertThat(found).isNotNull();
        assertThat(found.getOrderId()).isEqualTo(payment.getOrderId());
    }
}


