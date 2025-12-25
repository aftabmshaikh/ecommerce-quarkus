package com.ecommerce.order.controller;

import com.ecommerce.order.dto.shipping.ShippingOption;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/shipping")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Shipping API", description = "APIs for shipping operations")
public class ShippingController {

    // TODO: Inject ShippingService when implemented
    // @Inject
    // ShippingService shippingService;

    @GET
    @Path("/options")
    @Operation(summary = "Get available shipping options")
    public Response getShippingOptions(
            @QueryParam("country") String country,
            @QueryParam("postalCode") String postalCode) {
        // TODO: Implement shipping service
        // List<ShippingOption> options = shippingService.getAvailableShippingOptions(country, postalCode);
        // return Response.ok(options).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Shipping options functionality not yet implemented\"}")
                .build();
    }

    @POST
    @Path("/calculate")
    @Operation(summary = "Calculate shipping costs")
    public Response calculateShipping(
            @QueryParam("country") String country,
            @QueryParam("postalCode") String postalCode,
            @QueryParam("weight") Double weight,
            @QueryParam("value") Double value) {
        // TODO: Implement shipping service
        // List<ShippingOption> options = shippingService.calculateShippingOptions(country, postalCode, weight, value);
        // return Response.ok(options).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Shipping calculation functionality not yet implemented\"}")
                .build();
    }

    @GET
    @Path("/track/{trackingNumber}")
    @Operation(summary = "Track a shipment")
    public Response trackShipment(@PathParam("trackingNumber") String trackingNumber) {
        // TODO: Implement shipping service
        // Object trackingInfo = shippingService.trackShipment(trackingNumber);
        // return Response.ok(trackingInfo).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Shipment tracking functionality not yet implemented\"}")
                .build();
    }
}

