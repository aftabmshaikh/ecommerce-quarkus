package com.ecommerce.order.controller;

import com.ecommerce.order.dto.tracking.OrderStatusUpdate;
import com.ecommerce.order.dto.tracking.OrderTrackingResponse;
import jakarta.inject.Inject;
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
@Tag(name = "Order Tracking API", description = "APIs for order tracking")
public class OrderTrackingController {

    // TODO: Inject OrderTrackingService when implemented
    // @Inject
    // OrderTrackingService trackingService;

    @GET
    @Path("/{orderId}/tracking")
    @Operation(summary = "Get order tracking information")
    public Response getOrderTracking(@PathParam("orderId") UUID orderId) {
        // TODO: Implement tracking service
        // OrderTrackingResponse response = trackingService.getOrderTracking(orderId);
        // return Response.ok(response).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Order tracking functionality not yet implemented\"}")
                .build();
    }

    @GET
    @Path("/{orderId}/timeline")
    @Operation(summary = "Get order status timeline")
    public Response getOrderTimeline(@PathParam("orderId") UUID orderId) {
        // TODO: Implement tracking service
        // List<OrderStatusUpdate> timeline = trackingService.getOrderTimeline(orderId);
        // return Response.ok(timeline).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Order timeline functionality not yet implemented\"}")
                .build();
    }
}

