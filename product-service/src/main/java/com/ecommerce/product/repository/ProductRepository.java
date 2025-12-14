package com.ecommerce.product.repository;

import com.ecommerce.product.model.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, UUID> {

    public Optional<Product> findBySku(String sku) {
        return find("sku", sku).firstResultOptional();
    }

    public boolean existsBySku(String sku) {
        return count("sku", sku) > 0;
    }

    public List<Product> findByCategoryId(UUID categoryId) {
        return list("categoryId", categoryId);
    }

    public List<Product> findLowStockProducts(int threshold) {
        return list("stockQuantity <= ?1 and active = true", threshold);
    }

    public List<Product> searchProducts(String query) {
        String lowerCaseQuery = "%" + query.toLowerCase() + "%";
        return list("active = true AND (LOWER(name) LIKE ?1 OR LOWER(description) LIKE ?1)", lowerCaseQuery);
    }
}
