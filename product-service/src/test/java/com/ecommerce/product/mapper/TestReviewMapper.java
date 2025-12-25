package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.model.Review;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test-scoped CDI implementation of {@link ReviewMapper}.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class TestReviewMapper implements ReviewMapper {

    @Override
    public Review toEntity(ReviewRequest request) {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        review.setHelpfulCount(0);
        review.setNotHelpfulCount(0);
        review.setVerifiedPurchase(false);
        review.setCustomerEmail("anonymous@example.com");
        return review;
    }

    @Override
    public ReviewResponse toDto(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProductId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}


