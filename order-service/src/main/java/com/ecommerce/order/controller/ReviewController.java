package com.ecommerce.order.controller;

import com.ecommerce.order.dto.reviews.ReviewResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Review API", description = "APIs for product reviews")
public class ReviewController {

    // TODO: Inject ReviewService when implemented
    // @Inject
    // ReviewService reviewService;

    @GET
    @Path("/{productId}/reviews")
    @Operation(summary = "Get reviews for a product")
    public Response getProductReviews(
            @PathParam("productId") UUID productId,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("10") int pageSize) {
        // TODO: Implement review service
        // Page<ReviewResponse> reviews = reviewService.getProductReviews(productId, pageable);
        // return Response.ok(reviews).build();
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"message\":\"Product reviews functionality not yet implemented\"}")
                .build();
    }
}

