package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB-level tests that verify ProductRepository behaviour against a real Postgres DB
 * managed by Testcontainers and migrated with Flyway.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class ProductRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    ProductRepository productRepository;

    @Test
    @DisplayName("existsBySku returns true when SKU already present")
    @Transactional
    void existsBySku_trueWhenPresent() {
        Product product = new Product();
        product.setName("DB Product");
        product.setDescription("DB test");
        product.setSku("SKU-DB-1");
        product.setPrice(BigDecimal.valueOf(5.5));
        product.setStockQuantity(3);
        product.setCategoryId(null); // Category FK allows NULL
        product.setImageUrl("https://example.com/db.png");
        product.setActive(true);

        productRepository.persist(product);

        assertTrue(productRepository.existsBySku("SKU-DB-1"));
    }

    @Test
    @DisplayName("searchProducts finds by name or description")
    @Transactional
    void searchProducts_byNameOrDescription() {
        Product p1 = new Product();
        p1.setName("Gaming Mouse");
        p1.setDescription("High DPI mouse");
        p1.setSku("SKU-MOUSE-1");
        p1.setPrice(BigDecimal.valueOf(49.99));
        p1.setStockQuantity(20);
        p1.setCategoryId(null); // Category FK allows NULL
        p1.setImageUrl("https://example.com/mouse.png");
        p1.setActive(true);

        Product p2 = new Product();
        p2.setName("Office Keyboard");
        p2.setDescription("Silent keyboard");
        p2.setSku("SKU-KB-1");
        p2.setPrice(BigDecimal.valueOf(29.99));
        p2.setStockQuantity(15);
        p2.setCategoryId(null); // Category FK allows NULL
        p2.setImageUrl("https://example.com/keyboard.png");
        p2.setActive(true);

        productRepository.persist(p1);
        productRepository.persist(p2);

        List<Product> foundByName = productRepository.searchProducts("Mouse");
        List<Product> foundByDescription = productRepository.searchProducts("Silent");

        assertEquals("SKU-MOUSE-1", foundByName.get(0).getSku());
        assertEquals("SKU-KB-1", foundByDescription.get(0).getSku());
    }
}


