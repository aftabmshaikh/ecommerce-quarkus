package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoryServiceImpl implements CategoryService {

    @Inject
    CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.listAll();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
    }

    @Override
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    @Override
    public List<Category> getSubCategories(UUID parentId) {
        return categoryRepository.findSubCategories(parentId);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Set parent category if parentId is provided
        if (category.getParent() != null && category.getParent().getId() != null) {
            Category parent = getCategoryById(category.getParent().getId());
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        categoryRepository.persist(category);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(UUID id, Category categoryDetails) {
        Category category = getCategoryById(id);
        
        // Update category fields
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setImageUrl(categoryDetails.getImageUrl());
        category.setSlug(categoryDetails.getSlug());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setActive(categoryDetails.isActive());
        
        // Update parent category if needed
        if (categoryDetails.getParent() != null && categoryDetails.getParent().getId() != null) {
            if (!categoryDetails.getParent().getId().equals(id)) { // Prevent circular reference
                Category parent = getCategoryById(categoryDetails.getParent().getId());
                category.setParent(parent);
            }
        } else {
            category.setParent(null);
        }
        
        // No explicit persist needed for managed entities
        return category;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        
        // Check if category has any products
        if (categoryRepository.hasProducts(id)) {
            throw new IllegalStateException("Cannot delete category with existing products");
        }
        
        // Check if category has subcategories
        List<Category> subCategories = categoryRepository.findSubCategories(id);
        if (!subCategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories");
        }
        
        categoryRepository.delete(category);
    }

    @Override
    @Transactional
    public Category updateCategoryStatus(UUID id, boolean active) {
        Category category = getCategoryById(id);
        category.setActive(active);
        // No explicit persist needed for managed entities
        return category;
    }
}
