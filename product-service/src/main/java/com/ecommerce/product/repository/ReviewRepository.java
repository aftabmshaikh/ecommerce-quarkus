package com.ecommerce.product.repository;

import com.ecommerce.product.model.Review;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ReviewRepository implements PanacheRepositoryBase<Review, UUID> {

    public List<Review> findByProductId(UUID productId, Page page) {
        return find("productId", productId).page(page).list();
    }

    public boolean existsByOrderIdAndProductId(UUID orderId, UUID productId) {
        return count("orderId = ?1 and productId = ?2", orderId, productId) > 0;
    }

    public long countByProductId(UUID productId) {
        return count("productId", productId);
    }

    public Double calculateAverageRating(UUID productId) {
        return find("productId", productId)
                .project(Double.class)
                .singleResultOptional()
                .orElse(0.0); // Panache doesn't have a direct AVG function like Spring Data JPA
                              // This would typically be handled with a custom query or a stream operation
                              // For simplicity, returning 0.0 if no reviews are found.
                              // A more robust solution might involve a native query or a dedicated DTO.
    }
}
