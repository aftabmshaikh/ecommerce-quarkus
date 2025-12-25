package com.ecommerce.notification.repository;

import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NotificationRepository}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class NotificationRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("Persist notification and query by recipient email")
    @Transactional
    void persistAndFindByRecipient() {
        Notification notification = new Notification();
        notification.setRecipientEmail("test@example.com");
        notification.setSubject("Test");
        notification.setContent("Test content");
        notification.setType(Notification.NotificationType.ORDER_CONFIRMATION);
        notification.setSent(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.persist(notification);

        Notification found = notificationRepository.find("recipientEmail", "test@example.com").firstResult();
        assertThat(found).isNotNull();
        assertThat(found.getRecipientEmail()).isEqualTo("test@example.com");
    }
}

