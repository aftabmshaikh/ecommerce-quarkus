package com.ecommerce.payment.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.opentest4j.TestAbortedException;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Starts a Kafka Testcontainer and wires its bootstrap servers
 * into Quarkus configuration for the payment-events channel.
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
            System.out.println("Docker is not available. Skipping tests that require Kafka Testcontainers.");
            throw new TestAbortedException("Docker is not available. Tests require Testcontainers.");
        }

        try {
            kafka = new KafkaContainer(KAFKA_IMAGE)
                    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                    .withStartupTimeout(Duration.ofMinutes(5))
                    .waitingFor(Wait.forListeningPort()
                            .withStartupTimeout(Duration.ofMinutes(5)));
            kafka.start();

            String bootstrapServers = kafka.getBootstrapServers();

            // Ensure Kafka is actually ready and the payment-events topic exists
            waitForKafkaReady(bootstrapServers);
            createTopics(bootstrapServers);

            Map<String, String> props = new HashMap<>();
            // Global Kafka bootstrap for SmallRye reactive messaging
            props.put("kafka.bootstrap.servers", bootstrapServers);
            System.setProperty("kafka.bootstrap.servers", bootstrapServers);

            // Configure outgoing payment-events channel
            props.put("mp.messaging.outgoing.payment-events.connector", "smallrye-kafka");
            props.put("mp.messaging.outgoing.payment-events.value.serializer", "io.quarkus.kafka.client.serialization.JsonbSerializer");
            props.put("mp.messaging.outgoing.payment-events.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            // Make Kafka blocking in tests to ensure messages are actually sent
            props.put("mp.messaging.outgoing.payment-events.waitForWriteCompletion", "true");

            return props;
        } catch (Exception e) {
            System.err.println("Failed to start Kafka container for payment-service: " + e.getMessage());
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
        System.clearProperty("kafka.bootstrap.servers");
    }

    private boolean isDockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void waitForKafkaReady(String bootstrapServers) throws Exception {
        int maxAttempts = 30;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try {
                Map<String, Object> config = new HashMap<>();
                config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
                config.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);

                try (AdminClient adminClient = AdminClient.create(config)) {
                    adminClient.listTopics().names().get(5, TimeUnit.SECONDS);
                    return; // Kafka is ready
                }
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw new RuntimeException("Kafka did not become ready after " + maxAttempts + " attempts", e);
                }
                Thread.sleep(2000); // Wait 2 seconds before retry
            }
        }
    }

    private void createTopics(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            NewTopic paymentEventsTopic = new NewTopic("payment-events", 1, (short) 1);
            adminClient.createTopics(java.util.Collections.singletonList(paymentEventsTopic))
                    .all().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("Warning: Could not create Kafka topic payment-events: " + e.getMessage());
        }
    }
}


