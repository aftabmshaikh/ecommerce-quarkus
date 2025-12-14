package com.ecommerce.apigateway.controller;

import com.ecommerce.apigateway.client.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Map;

@Path("/")
public class GatewayResource {

    @Inject
    @RestClient
    ProductServiceClient productServiceClient;

    @Inject
    @RestClient
    OrderServiceClient orderServiceClient;

    @Inject
    @RestClient
    UserServiceClient userServiceClient;

    @Inject
    @RestClient
    InventoryServiceClient inventoryServiceClient;

    @Inject
    @RestClient
    CartServiceClient cartServiceClient;

    @Inject
    @RestClient
    PaymentServiceClient paymentServiceClient;

    @Inject
    @RestClient
    NotificationServiceClient notificationServiceClient;

    @GET
    @Path("{path:.*}")
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Retry(maxRetries = 3, delay = 1000)
    @Fallback(fallbackMethod = "fallback")
    public Response proxyGet(@PathParam("path") String path, @Context UriInfo uriInfo) {
        String query = uriInfo.getRequestUri().getQuery();
        String fullPath = query == null ? path : path + "?" + query;

        if (path.startsWith("api/products")) {
            return productServiceClient.proxyGet(fullPath.substring("api/products".length()));
        } else if (path.startsWith("api/orders")) {
            return orderServiceClient.proxyGet(fullPath.substring("api/orders".length()));
        } else if (path.startsWith("api/users")) {
            return userServiceClient.proxyGet(fullPath.substring("api/users".length()));
        } else if (path.startsWith("api/inventory")) {
            return inventoryServiceClient.proxyGet(fullPath.substring("api/inventory".length()));
        } else if (path.startsWith("api/cart")) {
            return cartServiceClient.proxyGet(fullPath.substring("api/cart".length()));
        } else if (path.startsWith("api/payments")) {
            return paymentServiceClient.proxyGet(fullPath.substring("api/payments".length()));
        } else if (path.startsWith("api/notifications")) {
            return notificationServiceClient.proxyGet(fullPath.substring("api/notifications".length()));
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("{path:.*}")
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Retry(maxRetries = 3, delay = 1000)
    @Fallback(fallbackMethod = "fallback")
    public Response proxyPost(@PathParam("path") String path, String body, @Context UriInfo uriInfo) {
        String query = uriInfo.getRequestUri().getQuery();
        String fullPath = query == null ? path : path + "?" + query;

        if (path.startsWith("api/products")) {
            return productServiceClient.proxyPost(fullPath.substring("api/products".length()), body);
        } else if (path.startsWith("api/orders")) {
            return orderServiceClient.proxyPost(fullPath.substring("api/orders".length()), body);
        } else if (path.startsWith("api/users")) {
            return userServiceClient.proxyPost(fullPath.substring("api/users".length()), body);
        } else if (path.startsWith("api/inventory")) {
            return inventoryServiceClient.proxyPost(fullPath.substring("api/inventory".length()), body);
        } else if (path.startsWith("api/cart")) {
            return cartServiceClient.proxyPost(fullPath.substring("api/cart".length()), body);
        } else if (path.startsWith("api/payments")) {
            return paymentServiceClient.proxyPost(fullPath.substring("api/payments".length()), body);
        } else if (path.startsWith("api/notifications")) {
            return notificationServiceClient.proxyPost(fullPath.substring("api/notifications".length()), body);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{path:.*}")
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Retry(maxRetries = 3, delay = 1000)
    @Fallback(fallbackMethod = "fallback")
    public Response proxyPut(@PathParam("path") String path, String body, @Context UriInfo uriInfo) {
        String query = uriInfo.getRequestUri().getQuery();
        String fullPath = query == null ? path : path + "?" + query;

        if (path.startsWith("api/products")) {
            return productServiceClient.proxyPut(fullPath.substring("api/products".length()), body);
        } else if (path.startsWith("api/orders")) {
            return orderServiceClient.proxyPut(fullPath.substring("api/orders".length()), body);
        } else if (path.startsWith("api/users")) {
            return userServiceClient.proxyPut(fullPath.substring("api/users".length()), body);
        } else if (path.startsWith("api/inventory")) {
            return inventoryServiceClient.proxyPut(fullPath.substring("api/inventory".length()), body);
        } else if (path.startsWith("api/cart")) {
            return cartServiceClient.proxyPut(fullPath.substring("api/cart".length()), body);
        } else if (path.startsWith("api/payments")) {
            return paymentServiceClient.proxyPut(fullPath.substring("api/payments".length()), body);
        } else if (path.startsWith("api/notifications")) {
            return notificationServiceClient.proxyPut(fullPath.substring("api/notifications".length()), body);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{path:.*}")
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Retry(maxRetries = 3, delay = 1000)
    @Fallback(fallbackMethod = "fallback")
    public Response proxyDelete(@PathParam("path") String path, @Context UriInfo uriInfo) {
        String query = uriInfo.getRequestUri().getQuery();
        String fullPath = query == null ? path : path + "?" + query;

        if (path.startsWith("api/products")) {
            return productServiceClient.proxyDelete(fullPath.substring("api/products".length()));
        } else if (path.startsWith("api/orders")) {
            return orderServiceClient.proxyDelete(fullPath.substring("api/orders".length()));
        } else if (path.startsWith("api/users")) {
            return userServiceClient.proxyDelete(fullPath.substring("api/users".length()));
        } else if (path.startsWith("api/inventory")) {
            return inventoryServiceClient.proxyDelete(fullPath.substring("api/inventory".length()));
        } else if (path.startsWith("api/cart")) {
            return cartServiceClient.proxyDelete(fullPath.substring("api/cart".length()));
        } else if (path.startsWith("api/payments")) {
            return paymentServiceClient.proxyDelete(fullPath.substring("api/payments".length()));
        } else if (path.startsWith("api/notifications")) {
            return notificationServiceClient.proxyDelete(fullPath.substring("api/notifications".length()));
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response fallback(String path, @Context UriInfo uriInfo) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of(
                        "status", "error",
                        "message", "Service is currently unavailable. Please try again later.",
                        "code", Response.Status.SERVICE_UNAVAILABLE.getStatusCode()
                ))
                .build();
    }
}
