package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService.StockUpdateEvent;
import com.ecommerce.product.testsupport.KafkaTestResource;
import com.ecommerce.product.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests that verify ProductService emits Kafka events to the configured topics.
 *
 * NOTE: These tests rely on Testcontainers Kafka and consume directly from the topics
 * that are configured for the application.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(KafkaTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class ProductServiceKafkaTest {

    static boolean isDockerNotAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    ProductService productService;

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    private String getKafkaBootstrapServers() {
        // KafkaTestResource sets this as a system property
        // Try system property first, then environment variable, then default
        String bootstrapServers = System.getProperty("kafka.bootstrap.servers");
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        }
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            bootstrapServers = "localhost:9092";
        }
        return bootstrapServers;
    }

    private KafkaConsumer<String, String> createConsumer(String topic) {
        Properties props = new Properties();
        String bootstrapServers = getKafkaBootstrapServers();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "product-service-kafka-test-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // Start from latest to only read new messages
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "10000");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        
        // Wait for partition assignment - topics should exist now (created by KafkaTestResource)
        // Poll until we get partition assignment
        int attempts = 0;
        while (consumer.assignment().isEmpty() && attempts < 50) {
            consumer.poll(Duration.ofMillis(200));
            attempts++;
        }
        
        // With AUTO_OFFSET_RESET_CONFIG set to "latest", the consumer will automatically
        // start from the latest offset, so we don't need to seek
        // This ensures we only read new messages produced after the consumer is ready
        
        return consumer;
    }

    private ConsumerRecord<String, String> pollForRecord(KafkaConsumer<String, String> consumer, String topic, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));
            Iterator<ConsumerRecord<String, String>> iterator = records.records(topic).iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            // Wait a bit before next poll to allow message to be produced and topic to be ready
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for Kafka message", e);
            }
        }
        throw new AssertionError("No message received from topic " + topic + " after " + maxAttempts + " attempts");
    }

    @Test
    @DisplayName("createProduct emits product-events message")
    void createProduct_emitsProductEvent() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Kafka Product")
                .description("Kafka event emission test")
                .sku("SKU-KAFKA-1")
                .price(BigDecimal.valueOf(9.99))
                .stockQuantity(5)
                .categoryId(null) // Category is optional, use null to avoid FK constraint
                .imageUrl("https://example.com/kafka.png")
                .active(true)
                .build();

        // Create consumer before sending the event to ensure we don't miss it
        try (KafkaConsumer<String, String> consumer = createConsumer("product-events")) {
            // Wait a bit to ensure consumer is fully ready and positioned at the latest offset
            Thread.sleep(1000);
            
            // Send the event
            productService.createProduct(request);
            
            // Wait for transaction to commit and message to be produced
            // Transaction synchronization happens after commit, so we need to wait
            Thread.sleep(3000);
            
            // Poll for the record (with retries)
            ConsumerRecord<String, String> record = pollForRecord(consumer, "product-events", 20);
            assertNotNull(record, "Expected a record from product-events topic");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            Product product = mapper.readValue(record.value(), Product.class);

            assertEquals(request.getName(), product.getName());
            assertEquals(request.getSku(), product.getSku());
        }
    }

    @Test
    @DisplayName("updateStock emits inventory-updates message")
    void updateStock_emitsInventoryUpdateEvent() throws Exception {
        // Arrange: create a product directly in the repository (within a transaction)
        Product product = new Product();
        product.setName("Inventory Product");
        product.setDescription("Inventory update test");
        product.setSku("SKU-INVENTORY-1");
        product.setPrice(BigDecimal.valueOf(15.0));
        product.setStockQuantity(10);
        product.setCategoryId(null); // Category is optional, use null to avoid FK constraint
        product.setImageUrl("https://example.com/inventory.png");
        product.setActive(true);
        
        // Use a transactional method to persist the product
        persistProductInTransaction(product);

        int delta = -3;
        int expectedNewStock = product.getStockQuantity() + delta;

        // Create consumer before sending the event to ensure we don't miss it
        try (KafkaConsumer<String, String> consumer = createConsumer("inventory-updates")) {
            // Send the event
            productService.updateStock(product.getId(), delta);
            
            // Wait for transaction to commit and message to be produced
            // Transaction synchronization happens after commit, so we need to wait
            Thread.sleep(3000);
            
            // Poll for the record (with retries)
            ConsumerRecord<String, String> record = pollForRecord(consumer, "inventory-updates", 20);
            assertNotNull(record, "Expected a record from inventory-updates topic");

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            StockUpdateEvent event = mapper.readValue(record.value(), StockUpdateEvent.class);

            assertEquals(product.getId(), event.productId());
            assertEquals(delta, event.quantityChange());
            assertEquals(expectedNewStock, event.newStock());
        }
    }

    @Transactional
    void persistProductInTransaction(Product product) {
        productRepository.persist(product);
        productRepository.flush();
    }
}


