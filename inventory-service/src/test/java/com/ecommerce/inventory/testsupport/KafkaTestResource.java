package com.ecommerce.inventory.testsupport;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.opentest4j.TestAbortedException;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Starts a Kafka Testcontainer and wires its bootstrap servers
 * into Quarkus configuration for reactive messaging.
 * 
 * If Docker is not available, this resource will skip gracefully and return
 * an empty configuration map, allowing tests to be skipped or use default configuration.
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
            kafka = new KafkaContainer(KAFKA_IMAGE)
                    .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                    .withStartupTimeout(Duration.ofMinutes(5))
                    .waitingFor(Wait.forListeningPort()
                            .withStartupTimeout(Duration.ofMinutes(5)));
            kafka.start();
            
            // Additional verification: wait for Kafka to be actually ready by trying to connect
            waitForKafkaReady(kafka.getBootstrapServers());

            String bootstrapServers = kafka.getBootstrapServers();
            
            // Explicitly create topics to ensure they exist before tests run
            createTopics(bootstrapServers);
            
            Map<String, String> props = new HashMap<>();
            // Global Kafka bootstrap for SmallRye reactive messaging
            props.put("kafka.bootstrap.servers", bootstrapServers);
            // Also set as system property for plain Kafka consumers in tests
            System.setProperty("kafka.bootstrap.servers", bootstrapServers);
            
            // Override incoming channels to use StringDeserializer instead of JsonbDeserializer
            // JsonbDeserializer requires a type parameter which causes issues when there are no @Incoming consumers
            // Using StringDeserializer prevents initialization errors
            props.put("mp.messaging.incoming.order-events.connector", "smallrye-kafka");
            props.put("mp.messaging.incoming.order-events.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("mp.messaging.incoming.order-events.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            
            props.put("mp.messaging.incoming.product-events.connector", "smallrye-kafka");
            props.put("mp.messaging.incoming.product-events.value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("mp.messaging.incoming.product-events.key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            
            // Configure outgoing channel
            props.put("mp.messaging.outgoing.inventory-events.connector", "smallrye-kafka");
            props.put("mp.messaging.outgoing.inventory-events.value.serializer", "io.quarkus.kafka.client.serialization.JsonbSerializer");
            props.put("mp.messaging.outgoing.inventory-events.key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            // Make Kafka blocking in tests to ensure messages are actually sent
            props.put("mp.messaging.outgoing.inventory-events.waitForWriteCompletion", "true");
            
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
        // Clean up system property
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

    private void waitForKafkaReady(String bootstrapServers) {
        // Wait for Kafka to be ready by attempting to create an AdminClient
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

    private void createTopics(String bootstrapServers) {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        try (AdminClient adminClient = AdminClient.create(config)) {
            // Create topics with 1 partition and replication factor 1 (suitable for testcontainers)
            NewTopic orderEventsTopic = new NewTopic("order-events", 1, (short) 1);
            NewTopic productEventsTopic = new NewTopic("product-events", 1, (short) 1);
            NewTopic inventoryEventsTopic = new NewTopic("inventory-events", 1, (short) 1);
            
            // Create all topics in a single call
            adminClient.createTopics(java.util.Arrays.asList(orderEventsTopic, productEventsTopic, inventoryEventsTopic))
                    .all().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            // If topic creation fails, log but don't fail - topics might already exist or auto-creation might handle it
            System.err.println("Warning: Could not create Kafka topics: " + e.getMessage());
        }
    }
}

