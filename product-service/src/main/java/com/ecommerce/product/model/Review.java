package com.ecommerce.product.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(nullable = false)
    private String customerName;
    
    @Column(nullable = false)
    private String customerEmail;
    
    @Column(nullable = false)
    private Integer rating;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(length = 1000)
    private String comment;
    
    @ElementCollection
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> imageUrls = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "tag")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> tags = new ArrayList<>();
    
    @Column(name = "is_verified_purchase", nullable = false)
    private boolean isVerifiedPurchase = false;
    
    @Column(name = "is_helpful_count", columnDefinition = "integer default 0")
    private int helpfulCount = 0;
    
    @Column(name = "is_not_helpful_count", columnDefinition = "integer default 0")
    private int notHelpfulCount = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void markHelpful() {
        this.helpfulCount++;
    }
    
    public void markNotHelpful() {
        this.notHelpfulCount++;
    }
    
    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }
    
    public void addTag(String tag) {
        this.tags.add(tag);
    }
}
