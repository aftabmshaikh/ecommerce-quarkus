package com.ecommerce.product.repository;

import com.ecommerce.product.model.Category;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CategoryRepository implements PanacheRepositoryBase<Category, UUID> {

    @Inject
    EntityManager entityManager;

    public Optional<Category> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public Optional<Category> findBySlug(String slug) {
        return find("slug", slug).firstResultOptional();
    }

    public List<Category> findByParentId(UUID parentId) {
        return list("parent.id", parentId);
    }

    public List<Category> findByActiveTrue() {
        return list("active", true);
    }

    public List<Category> findRootCategories() {
        return list("parent is null");
    }

    public List<Category> findSubCategories(UUID parentId) {
        return list("parent.id", parentId);
    }

    public boolean existsByName(String name) {
        return count("name", name) > 0;
    }

    public boolean existsBySlug(String slug) {
        return count("slug", slug) > 0;
    }

    public boolean hasProducts(UUID categoryId) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId", Long.class)
            .setParameter("categoryId", categoryId)
            .getSingleResult();
        return count != null && count > 0;
    }
}
