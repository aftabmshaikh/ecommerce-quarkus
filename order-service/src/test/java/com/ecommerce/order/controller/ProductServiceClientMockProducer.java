package com.ecommerce.order.controller;

import com.ecommerce.order.client.ProductServiceClient;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

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
            public Response checkProductStock(java.util.UUID productId, int quantity) {
                return Response.ok(true).build();
            }

            @Override
            public Response updateProductStocks(Map<java.util.UUID, Integer> productStocks) {
                return Response.ok().build();
            }

            @Override
            public Response isProductAvailable(java.util.UUID productId) {
                return Response.ok(true).build();
            }

            @Override
            public Response updateProductStock(com.ecommerce.order.dto.ProductStockUpdateRequest request) {
                return Response.ok().build();
            }

            @Override
            public Response checkStockAvailability(List<Map<String, Object>> items) {
                return Response.ok().build();
            }

            @Override
            public Response updateInventory(List<Map<String, Object>> inventoryUpdates) {
                return Response.ok().build();
            }
        };
    }
}

