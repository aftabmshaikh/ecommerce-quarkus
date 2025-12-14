package com.ecommerce.apigateway;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/orders")
@RegisterRestClient(configKey = "order-service")
public interface OrderResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response createOrder(String order);

    @GET
    @Path("/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getOrderById(@PathParam("orderId") Long orderId);
}