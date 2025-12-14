package com.ecommerce.product.controller;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.dto.review.ReviewStatsResponse;
import com.ecommerce.product.service.ReviewService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/products/{productId}/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Review API", description = "APIs for managing product reviews")
public class ReviewController {

    @Inject
    ReviewService reviewService;

    @POST
    @Operation(summary = "Submit a product review")
    public Response submitReview(
            @HeaderParam("X-User-Id") UUID userId,
            @PathParam("productId") UUID productId,
            @Valid ReviewRequest request) {
        request.setProductId(productId);
        ReviewResponse response = reviewService.createReview(userId, request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Operation(summary = "Get reviews for a product")
    public List<ReviewResponse> getProductReviews(
            @PathParam("productId") UUID productId,
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("10") int pageSize) {
        return reviewService.getReviewsByProductId(productId, pageIndex, pageSize);
    }

    @GET
    @Path("/{reviewId}")
    @Operation(summary = "Get review by ID")
    public ReviewResponse getReview(
            @PathParam("productId") UUID productId,
            @PathParam("reviewId") UUID reviewId) {
        return reviewService.getReviewById(reviewId);
    }

    @POST
    @Path("/{reviewId}/helpful")
    @Operation(summary = "Mark a review as helpful")
    public Response markHelpful(
            @PathParam("productId") UUID productId,
            @PathParam("reviewId") UUID reviewId) {
        reviewService.markHelpful(reviewId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{reviewId}/not-helpful")
    @Operation(summary = "Mark a review as not helpful")
    public Response markNotHelpful(
            @PathParam("productId") UUID productId,
            @PathParam("reviewId") UUID reviewId) {
        reviewService.markNotHelpful(reviewId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{reviewId}")
    @Operation(summary = "Delete a review")
    public Response deleteReview(
            @HeaderParam("X-User-Id") UUID userId,
            @PathParam("productId") UUID productId,
            @PathParam("reviewId") UUID reviewId) {
        reviewService.deleteReview(reviewId, userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get review statistics for a product")
    public ReviewStatsResponse getReviewStats(@PathParam("productId") UUID productId) {
        double averageRating = reviewService.getAverageRating(productId);
        long reviewCount = reviewService.getReviewCount(productId);
        
        return ReviewStatsResponse.builder()
            .productId(productId)
            .averageRating(averageRating)
            .totalReviews(reviewCount)
            .ratingDistribution(reviewService.getRatingDistribution(productId))
            .build();
    }
}
