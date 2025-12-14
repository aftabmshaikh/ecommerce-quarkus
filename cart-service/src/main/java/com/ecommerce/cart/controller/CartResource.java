package com.ecommerce.cart.controller;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;

@Path("/api/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    @Inject
    CartRepository cartRepository;

    @GET
    @Path("/{userId}")
    public Response getCart(@PathParam("userId") UUID userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(c -> Response.ok(c).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/{userId}")
    @Transactional
    public Response addOrUpdateCart(@PathParam("userId") UUID userId, Cart cart) {
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            Cart c = existingCart.get();
            c.setItems(cart.getItems());
            cartRepository.persist(c);
        } else {
            cart.setUserId(userId);
            cartRepository.persist(cart);
        }
        return Response.ok(cart).build();
    }

    @DELETE
    @Path("/{userId}")
    @Transactional
    public Response deleteCart(@PathParam("userId") UUID userId) {
        cartRepository.deleteByUserId(userId);
        return Response.noContent().build();
    }
}