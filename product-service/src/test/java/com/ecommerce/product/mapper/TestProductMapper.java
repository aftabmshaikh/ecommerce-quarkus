package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.model.Product;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test-scoped CDI implementation of {@link ProductMapper}.
 *
 * Marked as {@link Alternative} with a priority so it is preferred over any
 * MapStruct-generated mapper if present, ensuring CDI always has a mapper bean.
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class TestProductMapper implements ProductMapper {

    @Override
    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        updateProductFromRequest(request, product);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    @Override
    public ProductResponse toDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategoryId())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .build();
    }

    @Override
    public void updateProductFromRequest(ProductRequest request, Product product) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategoryId(request.getCategoryId());
        product.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
    }
}


