package com.ecommerce.order.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.UUID;

@RegisterRestClient(configKey = "customer-service")
@Path("/api/customers")
public interface CustomerServiceClient {

    @GET
    @Path("/{customerId}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    boolean customerExists(@PathParam("customerId") UUID customerId);

    @GET
    @Path("/{customerId}/has-purchased-product/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    boolean hasPurchasedProduct(
            @PathParam("customerId") UUID customerId,
            @PathParam("productId") UUID productId,
            @HeaderParam("Authorization") String token);
}
