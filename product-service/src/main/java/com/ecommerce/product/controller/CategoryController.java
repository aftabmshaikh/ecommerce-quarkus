package com.ecommerce.product.controller;

import com.ecommerce.product.model.Category;
import com.ecommerce.product.service.CategoryService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Category API", description = "APIs for managing product categories")
public class CategoryController {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(summary = "Get all categories")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get category by ID")
    public Category getCategoryById(@PathParam("id") UUID id) {
        return categoryService.getCategoryById(id);
    }

    @GET
    @Path("/slug/{slug}")
    @Operation(summary = "Get category by slug")
    public Category getCategoryBySlug(@PathParam("slug") String slug) {
        return categoryService.getCategoryBySlug(slug);
    }

    @GET
    @Path("/roots")
    @Operation(summary = "Get all root categories (categories with no parent)")
    public List<Category> getRootCategories() {
        return categoryService.getRootCategories();
    }

    @GET
    @Path("/{parentId}/subcategories")
    @Operation(summary = "Get all subcategories of a parent category")
    public List<Category> getSubCategories(@PathParam("parentId") UUID parentId) {
        return categoryService.getSubCategories(parentId);
    }

    @POST
    @Operation(summary = "Create a new category")
    public Response createCategory(@Valid Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return Response.status(Response.Status.CREATED).entity(createdCategory).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing category")
    public Category updateCategory(
            @PathParam("id") UUID id,
            @Valid Category categoryDetails) {
        return categoryService.updateCategory(id, categoryDetails);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a category")
    public Response deleteCategory(@PathParam("id") UUID id) {
        categoryService.deleteCategory(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/status")
    @Operation(summary = "Toggle category status (active/inactive)")
    public Category toggleCategoryStatus(
            @PathParam("id") UUID id,
            @QueryParam("active") boolean active) {
        return categoryService.updateCategoryStatus(id, active);
    }
}
