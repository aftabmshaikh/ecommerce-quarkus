package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import com.ecommerce.user.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserRepository}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class UserRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    UserRepository userRepository;

    @Test
    @DisplayName("Persist user and find by username")
    @Transactional
    void persistAndFindByUsername() {
        User user = new User();
        user.setUsername("test-user");
        user.setEmail("test@example.com");
        user.setPassword("secret");

        userRepository.persist(user);

        User found = userRepository.find("username", "test-user").firstResult();
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("test-user");
    }
}


