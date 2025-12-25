package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Order API", description = "APIs for managing orders")
public class OrderController {

    @Inject
    OrderService orderService;

    @POST
    @Operation(summary = "Create a new order")
    public Response createOrder(@Valid OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{orderId}")
    @Operation(summary = "Get order by ID")
    public OrderResponse getOrderById(@PathParam("orderId") UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    @GET
    @Path("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public OrderResponse getOrderByNumber(@PathParam("orderNumber") String orderNumber) {
        return orderService.getOrderByNumber(orderNumber);
    }

    @GET
    @Path("/customer/{customerId}")
    @Operation(summary = "Get all orders for a customer")
    public List<OrderResponse> getCustomerOrders(
            @PathParam("customerId") UUID customerId,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("10") int pageSize) {
        return orderService.getCustomerOrders(customerId, pageIndex, pageSize);
    }

    @PUT
    @Path("/{orderId}/status")
    @Operation(summary = "Update order status")
    public OrderResponse updateOrderStatus(
            @PathParam("orderId") UUID orderId,
            @QueryParam("status") OrderStatus status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @POST
    @Path("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public OrderResponse cancelOrder(@PathParam("orderId") UUID orderId) {
        return orderService.cancelOrder(orderId);
    }

    @GET
    @Path("/health")
    @Operation(summary = "Health check endpoint")
    public Response health() {
        return Response.ok("Order Service is healthy").build();
    }

    // Review endpoints
    @POST
    @Path("/{orderId}/reviews")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submit a review for an order item")
    public Response submitReview(
            @PathParam("orderId") UUID orderId,
            com.ecommerce.order.dto.reviews.ReviewRequest request) {
        // TODO: Implement review service
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Review submission functionality not yet implemented\"}")
                .build();
    }

    @GET
    @Path("/{orderId}/reviews")
    @Operation(summary = "Get reviews for an order")
    public Response getOrderReviews(@PathParam("orderId") UUID orderId) {
        // TODO: Implement review service
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Order reviews functionality not yet implemented\"}")
                .build();
    }
}
