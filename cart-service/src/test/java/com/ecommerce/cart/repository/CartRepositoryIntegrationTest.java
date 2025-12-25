package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.testsupport.PostgresTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.testcontainers.DockerClientFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration tests for {@link CartRepository}.
 */
@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
@DisabledIf(value = "isDockerNotAvailable", disabledReason = "Docker is not available. Tests require Testcontainers.")
class CartRepositoryIntegrationTest {

    static boolean isDockerNotAvailable() {
        try {
            DockerClientFactory.instance().client();
            return false; // Docker is available
        } catch (Exception e) {
            return true; // Docker is not available
        }
    }

    @Inject
    CartRepository cartRepository;

    @Test
    @DisplayName("Persist cart with items and load by userId")
    @Transactional
    void persistAndFindByUserId() {
        Cart cart = new Cart();
        cart.setUserId(UUID.randomUUID());

        CartItem item = new CartItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName("Test Product");
        item.setUnitPrice(java.math.BigDecimal.valueOf(10.99));
        item.setQuantity(2);
        item.setCart(cart);

        cart.getItems().add(item);

        cartRepository.persist(cart);

        Cart found = cartRepository.find("userId", cart.getUserId()).firstResult();

        assertNotNull(found);
        assertEquals(1, found.getItems().size());
    }
}


