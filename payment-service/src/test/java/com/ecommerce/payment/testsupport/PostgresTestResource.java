package com.ecommerce.payment.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.opentest4j.TestAbortedException;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

/**
 * Testcontainers-backed Postgres for payment-service integration tests.
 *
 * If Docker is not available, this resource will skip gracefully.
 */
public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName POSTGRES_IMAGE =
            DockerImageName.parse("postgres:15-alpine");

    private PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        // Check if Docker is available
        if (!isDockerAvailable()) {
            System.out.println("Docker is not available. Skipping tests that require Testcontainers.");
            throw new TestAbortedException("Docker is not available. Tests require Testcontainers.");
        }

        try {
            postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                    .withDatabaseName("paymentdb")
                    .withUsername("payment")
                    .withPassword("payment");

            postgres.start();

            Map<String, String> props = new HashMap<>();
            props.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
            props.put("quarkus.datasource.username", postgres.getUsername());
            props.put("quarkus.datasource.password", postgres.getPassword());
            props.put("quarkus.flyway.migrate-at-start", "true");

            return props;
        } catch (Exception e) {
            System.err.println("Failed to start PostgreSQL container: " + e.getMessage());
            throw new TestAbortedException("Failed to start PostgreSQL container: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (postgres != null) {
            try {
                postgres.stop();
            } catch (Exception e) {
                // Ignore errors during stop
            }
        }
    }

    private boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


