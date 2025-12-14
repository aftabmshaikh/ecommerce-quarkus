package com.ecommerce.product.dto.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Order ID is required")
    private UUID orderId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;
    
    @NotBlank(message = "Comment is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;
    
    private List<String> imageUrls;
    
    private List<String> tags;
}
