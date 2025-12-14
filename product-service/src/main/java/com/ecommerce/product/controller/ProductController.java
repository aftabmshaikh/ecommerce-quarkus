package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.service.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product API", description = "APIs for managing products")
public class ProductController {

    @Inject
    ProductService productService;

    @POST
    @Operation(summary = "Create a new product")
    public Response createProduct(@Valid ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get product by ID")
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Retry(maxRetries = 3, delay = 1000)
    @Fallback(fallbackMethod = "getProductFallback")
    public ProductResponse getProductById(@PathParam("id") UUID id) {
        return productService.getProductById(id);
    }

    @GET
    @Operation(summary = "Get all products with pagination")
    public List<ProductResponse> getAllProducts(
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("10") int pageSize) {
        return productService.getAllProducts(pageIndex, pageSize);
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search products by name or description")
    public List<ProductResponse> searchProducts(@QueryParam("query") String query) {
        return productService.searchProducts(query);
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update a product")
    public ProductResponse updateProduct(
            @PathParam("id") UUID id,
            @Valid ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a product")
    public Response deleteProduct(@PathParam("id") UUID id) {
        productService.deleteProduct(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/stock")
    @Operation(summary = "Update product stock")
    public ProductResponse updateStock(
            @PathParam("id") UUID id,
            @QueryParam("quantity") int quantity) {
        return productService.updateStock(id, quantity);
    }

    @POST
    @Path("/batch")
    @Operation(summary = "Get multiple products by IDs")
    public List<ProductResponse> getProductsByIds(List<UUID> productIds) {
        return productService.getProductsByIds(productIds);
    }

    @GET
    @Path("/{id}/stock")
    @Operation(summary = "Check if product is in stock")
    public Response isInStock(
            @PathParam("id") UUID id,
            @QueryParam("quantity") @DefaultValue("1") int quantity) {
        boolean inStock = productService.isInStock(id, quantity);
        return Response.ok(inStock).build();
    }

    // Fallback method for circuit breaker
    public ProductResponse getProductFallback(UUID id, Throwable t) {
        // Log the exception for debugging
        // LOG.error("Fallback for getProductById for ID: " + id, t);
        // Return a default response or fetch from cache
        return ProductResponse.builder()
                .id(id)
                .name("Product information is not available at the moment")
                .description("Please try again later")
                .price(BigDecimal.ZERO)
                .stockQuantity(0)
                .active(false)
                .build();
    }
}
