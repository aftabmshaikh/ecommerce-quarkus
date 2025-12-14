package com.ecommerce.apigateway;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/inventory")
@RegisterRestClient(configKey = "inventory-service")
public interface InventoryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getInventory(@QueryParam("productIds") String productIds);
}