package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.review.ReviewRequest;
import com.ecommerce.product.dto.review.ReviewResponse;
import com.ecommerce.product.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(componentModel = "cdi", imports = {LocalDateTime.class})
public interface ReviewMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "helpfulCount", constant = "0"),
            @Mapping(target = "notHelpfulCount", constant = "0"),
            @Mapping(target = "isVerifiedPurchase", constant = "false"),
            @Mapping(target = "customerEmail", expression = "java(\"anonymous@example.com\")")
    })
    Review toEntity(ReviewRequest request);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "productId", source = "productId"),
            @Mapping(target = "userId", source = "customerId"),
            @Mapping(target = "userName", source = "customerName"),
            @Mapping(target = "rating", source = "rating"),
            @Mapping(target = "comment", source = "comment"),
            @Mapping(target = "createdAt", source = "createdAt"),
            @Mapping(target = "updatedAt", source = "updatedAt")
    })
    ReviewResponse toDto(Review review);
}