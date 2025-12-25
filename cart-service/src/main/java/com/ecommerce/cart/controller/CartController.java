package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.service.CartService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cart API", description = "APIs for managing shopping cart")
public class CartController {

    @Inject
    CartService cartService;

    @GET
    @Operation(summary = "Get the current user's cart")
    public CartResponse getCart(@HeaderParam("X-User-Id") UUID userId) {
        return cartService.getOrCreateCart(userId);
    }

    @POST
    @Path("/items")
    @Operation(summary = "Add an item to the cart")
    public CartResponse addToCart(
            @HeaderParam("X-User-Id") UUID userId,
            @Valid CartItemRequest request) {
        return cartService.addItemToCart(userId, request);
    }

    @PUT
    @Path("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public CartResponse updateCartItem(
            @HeaderParam("X-User-Id") UUID userId,
            @PathParam("itemId") String itemId,
            @QueryParam("quantity") int quantity) {
        return cartService.updateCartItem(userId, itemId, quantity);
    }

    @DELETE
    @Path("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public CartResponse removeFromCart(
            @HeaderParam("X-User-Id") UUID userId,
            @PathParam("itemId") String itemId) {
        return cartService.removeItemFromCart(userId, itemId);
    }

    @DELETE
    @Operation(summary = "Clear the cart")
    public Response clearCart(@HeaderParam("X-User-Id") UUID userId) {
        cartService.clearCart(userId);
        return Response.noContent().build();
    }
}
