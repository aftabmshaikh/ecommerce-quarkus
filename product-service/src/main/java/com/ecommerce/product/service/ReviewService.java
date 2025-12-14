package com.ecommerce.product.service;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.exception.ReviewAlreadyExistsException;
import com.ecommerce.product.exception.ReviewNotFoundException;
import com.ecommerce.product.exception.UnauthorizedAccessException;
import com.ecommerce.product.mapper.ReviewMapper;
import com.ecommerce.product.model.Review;
import com.ecommerce.product.repository.ReviewRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class ReviewService {

    private static final Logger log = Logger.getLogger(ReviewService.class);

    @Inject
    ReviewRepository reviewRepository;

    @Inject
    ProductService productService;

    @Inject
    ReviewMapper reviewMapper;

    @Transactional
    @CacheInvalidateAll(cacheName = "products") // Invalidate product cache when reviews change
    public ReviewResponse createReview(UUID customerId, ReviewRequest request) {
        // Check if review already exists for this order and product
        if (request.getOrderId() != null &&
            reviewRepository.existsByOrderIdAndProductId(request.getOrderId(), request.getProductId())) {
            throw new ReviewAlreadyExistsException(
                "A review already exists for this product in the specified order"
            );
        }

        // Create and save the review
        Review review = reviewMapper.toEntity(request);
        review.setCustomerId(customerId);
        // In a real app, you would fetch customer details from the user service
        review.setCustomerName("Anonymous");
        
        reviewRepository.persist(review);
        
        // Update product rating stats
        productService.updateProductRating(request.getProductId());
        
        return reviewMapper.toDto(review);
    }

    @CacheResult(cacheName = "reviews-by-product")
    public List<ReviewResponse> getReviewsByProductId(UUID productId, int pageIndex, int pageSize) {
        return reviewRepository.findByProductId(productId, Page.of(pageIndex, pageSize))
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @CacheResult(cacheName = "reviews")
    public ReviewResponse getReviewById(UUID reviewId) {
        return reviewRepository.findByIdOptional(reviewId)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "reviews")
    @CacheInvalidateAll(cacheName = "reviews-by-product")
    public void markHelpful(UUID reviewId) {
        Review review = reviewRepository.findByIdOptional(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        // No explicit persist needed for managed entities
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "reviews")
    @CacheInvalidateAll(cacheName = "reviews-by-product")
    public void markNotHelpful(UUID reviewId) {
        Review review = reviewRepository.findByIdOptional(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        review.setNotHelpfulCount(review.getNotHelpfulCount() + 1);
        // No explicit persist needed for managed entities
    }

    @CacheResult(cacheName = "average-rating")
    public double getAverageRating(UUID productId) {
        Double avgRating = reviewRepository.calculateAverageRating(productId);
        return avgRating != null ? avgRating : 0.0;
    }

    @CacheResult(cacheName = "review-count")
    public long getReviewCount(UUID productId) {
        return reviewRepository.countByProductId(productId);
    }
    
    @CacheResult(cacheName = "rating-distribution")
    public Map<Integer, Long> getRatingDistribution(UUID productId) {
        // In a real app, you would implement this to return the count of reviews for each rating (1-5)
        // For now, we'll return a map with all ratings set to 0
        return IntStream.rangeClosed(1, 5)
                .boxed()
                .collect(Collectors.toMap(
                        rating -> rating,
                        rating -> 0L
                ));
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "reviews")
    @CacheInvalidateAll(cacheName = "reviews-by-product")
    @CacheInvalidateAll(cacheName = "products") // Invalidate product cache when reviews change
    public void deleteReview(UUID reviewId, UUID customerId) {
        Review review = reviewRepository.findByIdOptional(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));
        
        if (!review.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this review");
        }
        
        reviewRepository.delete(review);
        // Update product rating stats
        productService.updateProductRating(review.getProductId());
    }
}
