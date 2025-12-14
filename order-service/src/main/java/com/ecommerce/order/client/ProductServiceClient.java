package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductStockUpdateRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RegisterRestClient(configKey = "product-service")
@Path("/api/products")
public interface ProductServiceClient {

    @GET
    @Path("/{productId}/stock")
    @Produces(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "checkProductStockFallback")
    Response checkProductStock(@PathParam("productId") UUID productId, @QueryParam("quantity") int quantity);

    @POST
    @Path("/batch/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "updateProductStocksFallback")
    Response updateProductStocks(Map<UUID, Integer> productStocks);

    @GET
    @Path("/{productId}/available")
    @Produces(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "isProductAvailableFallback")
    Response isProductAvailable(@PathParam("productId") UUID productId);

    @PUT
    @Path("/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "updateProductStockFallback")
    Response updateProductStock(ProductStockUpdateRequest request);

    @POST
    @Path("/check-stock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "checkStockAvailabilityFallback")
    Response checkStockAvailability(List<Map<String, Object>> items);

    @POST
    @Path("/update-inventory")
    @Consumes(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "updateInventoryFallback")
    Response updateInventory(List<Map<String, Object>> inventoryUpdates);

    default Response checkProductStockFallback(UUID productId, int quantity) {
        return Response.ok(false).build();
    }

    default Response updateProductStocksFallback(Map<UUID, Integer> productStocks) {
        return Response.ok().build();
    }

    default Response isProductAvailableFallback(UUID productId) {
        return Response.ok(false).build();
    }

    default Response updateProductStockFallback(ProductStockUpdateRequest request) {
        return Response.ok().build();
    }

    default Response checkStockAvailabilityFallback(List<Map<String, Object>> items) {
        return Response.ok().build();
    }

    default Response updateInventoryFallback(List<Map<String, Object>> inventoryUpdates) {
        return Response.ok().build();
    }
}
