package com.ecommerce.cart.testsupport;

import com.ecommerce.cart.client.ProductServiceClient;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;

import java.util.Optional;
import java.util.UUID;

@Alternative
@Priority(1)
@ApplicationScoped
public class ProductServiceClientMockProducer {

    @Produces
    @ApplicationScoped
    @org.eclipse.microprofile.rest.client.inject.RestClient
    public ProductServiceClient produceMockProductServiceClient() {
        return new ProductServiceClient() {
            @Override
            public Optional<ProductServiceClient.ProductDto> getProductById(UUID productId) {
                return Optional.of(new ProductServiceClient.ProductDto(
                        productId,
                        "Test Product",
                        "Test Description",
                        "test-image.jpg",
                        10.99,
                        100
                ));
            }

            @Override
            public boolean isInStock(UUID productId, int quantity) {
                // Default to true for testing - can be overridden in tests
                return true;
            }
        };
    }
}

