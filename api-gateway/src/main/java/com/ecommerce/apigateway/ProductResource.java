package com.ecommerce.apigateway;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;

@Path("/api/products")
@RegisterRestClient(configKey = "product-service")
public interface ProductResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 1000, successThreshold = 3)
    @Fallback(fallbackMethod = "getProductsFallback")
    @Timeout(5000)
    Response getProducts(@QueryParam("page") int page, @QueryParam("size") int size);

    @GET
    @Path("/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 1000, successThreshold = 3)
    @Fallback(fallbackMethod = "getProductByIdFallback")
    @Timeout(5000)
    Response getProductById(@PathParam("productId") Long productId);

    default Response getProductsFallback(int page, int size) {
        return Response.ok(Collections.emptyList()).build();
    }

    default Response getProductByIdFallback(Long productId) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}