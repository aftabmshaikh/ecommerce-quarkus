package com.ecommerce.order.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.opentest4j.TestAbortedException;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Testcontainers-backed Kafka broker for order-service tests.
 * 
 * If Docker is not available, this resource will skip gracefully.
 */
public class KafkaTestResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName KAFKA_IMAGE =
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0");

    private KafkaContainer kafka;

    @Override
    public Map<String, String> start() {
        // Check if Docker is available
        if (!isDockerAvailable()) {
            System.out.println("Docker is not available. Skipping tests that require Testcontainers.");
            throw new TestAbortedException("Docker is not available. Tests require Testcontainers.");
        }

        try {
            // Use a connection-based wait strategy that verifies Kafka is actually ready
            // This is more reliable than waiting for log patterns which may vary by version
            kafka = new KafkaContainer(KAFKA_IMAGE)
                    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                    .withStartupTimeout(Duration.ofMinutes(5))
                    .waitingFor(Wait.forListeningPort()
                            .withStartupTimeout(Duration.ofMinutes(5)));
            kafka.start();
            
            // Additional verification: wait for Kafka to be actually ready by trying to connect
            waitForKafkaReady(kafka.getBootstrapServers());

            Map<String, String> props = new HashMap<>();
            props.put("kafka.bootstrap.servers", kafka.getBootstrapServers());
            return props;
        } catch (Exception e) {
            System.err.println("Failed to start Kafka container: " + e.getMessage());
            throw new TestAbortedException("Failed to start Kafka container: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (kafka != null) {
            try {
                kafka.stop();
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

    private void waitForKafkaReady(String bootstrapServers) {
        // Wait for Kafka to be ready by attempting to create an AdminClient
        // This is more reliable than waiting for log patterns
        int maxAttempts = 30;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                Map<String, Object> config = new HashMap<>();
                config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
                config.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
                
                try (AdminClient adminClient = AdminClient.create(config)) {
                    // Try to list topics to verify Kafka is ready
                    adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
                    return; // Kafka is ready
                }
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new RuntimeException("Kafka did not become ready after " + maxAttempts + " attempts", e);
                }
                try {
                    Thread.sleep(2000); // Wait 2 seconds before retry
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for Kafka", ie);
                }
            }
        }
    }
}


