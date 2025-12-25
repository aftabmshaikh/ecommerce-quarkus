package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.InventoryItem;
import com.ecommerce.inventory.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple integration tests for {@link InventoryRepository} backed by Testcontainers Postgres.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class InventoryRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Persist and find by skuCode")
    @Transactional
    void persistAndFindBySkuCode() {
        InventoryItem item = new InventoryItem();
        item.setProductId(UUID.randomUUID());
        item.setSkuCode("SKU-INV-1");
        item.setQuantity(10);
        item.setReservedQuantity(0);
        item.setLowStockThreshold(5);
        item.setRestockThreshold(10);
        item.setUnitCost(BigDecimal.TEN);
        item.setLocationCode("LOC-1");
        item.setBinLocation("BIN-1");
        item.setIsActive(true);

        inventoryRepository.persist(item);

        InventoryItem found = inventoryRepository.find("skuCode", "SKU-INV-1").firstResult();
        assertThat(found).isNotNull();
        assertThat(found.getSkuCode()).isEqualTo("SKU-INV-1");
    }
}


