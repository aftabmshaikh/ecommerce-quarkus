package com.ecommerce.product.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatsResponse {
    private UUID productId;
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingDistribution;
}
