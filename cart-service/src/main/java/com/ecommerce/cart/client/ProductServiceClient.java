package com.ecommerce.cart.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Optional;
import java.util.UUID;

@RegisterRestClient(configKey = "product-service")
@Path("/api/products")
public interface ProductServiceClient {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Optional<ProductDto> getProductById(@PathParam("id") UUID productId);
    
    @GET
    @Path("/{id}/stock")
    @Produces(MediaType.APPLICATION_JSON)
    boolean isInStock(
            @PathParam("id") UUID productId,
            @QueryParam("quantity") int quantity
    );
    
    record ProductDto(
            UUID id,
            String name,
            String description,
            String imageUrl,
            double price,
            int stock
    ) {}
}
