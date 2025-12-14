package com.ecommerce.apigateway.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "notification-service")
public interface NotificationServiceClient {

    @GET
    @Path("/{path:.*}")
    Response proxyGet(@PathParam("path") String path);

    @POST
    @Path("/{path:.*}")
    Response proxyPost(@PathParam("path") String path, String body);

    @PUT
    @Path("/{path:.*}")
    Response proxyPut(@PathParam("path") String path, String body);

    @DELETE
    @Path("/{path:.*}")
    Response proxyDelete(@PathParam("path") String path);
}
