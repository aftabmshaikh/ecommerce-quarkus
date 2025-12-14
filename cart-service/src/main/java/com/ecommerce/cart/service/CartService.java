package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.dto.CartItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.exception.CartNotFoundException;
import com.ecommerce.cart.exception.ProductNotAvailableException;
import com.ecommerce.cart.mapper.CartMapper;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@ApplicationScoped
public class CartService {

    private static final Logger log = Logger.getLogger(CartService.class);

    @Inject
    CartRepository cartRepository;

    @Inject
    @RestClient
    ProductServiceClient productServiceClient;

    @Inject
    CartMapper cartMapper;

    @Transactional
    public CartResponse getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .map(cartMapper::toDto)
                .orElseGet(() -> createNewCart(userId));
    }

    @Transactional
    public CartResponse addItemToCart(UUID userId, CartItemRequest request) {
        validateProductAvailability(request.getProductId(), request.getQuantity());
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCartEntity(userId));
        
        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + request.getQuantity()),
                        () -> addNewCartItem(cart, request)
                );
        
        cartRepository.persist(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public CartResponse updateCartItem(UUID userId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            return removeItemFromCart(userId, itemId);
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    validateProductAvailability(item.getProductId(), quantity);
                    item.setQuantity(quantity);
                });
        
        cartRepository.persist(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(UUID userId, UUID itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
        
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return createNewCart(userId);
        }
        
        cartRepository.persist(cart);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        cartRepository.deleteByUserId(userId);
    }

    private Cart createNewCartEntity(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cartRepository.persist(cart); // Persist to get an ID
        return cart;
    }

    private CartResponse createNewCart(UUID userId) {
        Cart newCart = createNewCartEntity(userId);
        return cartMapper.toDto(newCart);
    }

    private void addNewCartItem(Cart cart, CartItemRequest request) {
        var product = productServiceClient.getProductById(request.getProductId())
                .orElseThrow(() -> new ProductNotAvailableException("Product not found"));
        
        CartItem item = new CartItem();
        item.setProductId(request.getProductId());
        item.setProductName(product.name());
        item.setProductImage(product.imageUrl());
        item.setUnitPrice(BigDecimal.valueOf(product.price()).setScale(2, RoundingMode.HALF_UP));
        item.setQuantity(request.getQuantity());
        item.setCart(cart); // Set the parent cart
        
        cart.addItem(item);
    }

    private void validateProductAvailability(UUID productId, int quantity) {
        boolean isAvailable = productServiceClient.isInStock(productId, quantity);
        if (!isAvailable) {
            throw new ProductNotAvailableException("Product is not available in the requested quantity");
        }
    }
}
