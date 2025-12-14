package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.Cart;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CartRepository implements PanacheRepositoryBase<Cart, UUID> {

    public Optional<Cart> findByUserId(UUID userId) {
        return find("userId", userId).firstResultOptional();
    }

    @Transactional
    public long deleteByUserId(UUID userId) {
        return delete("userId", userId);
    }

    public boolean existsByUserId(UUID userId) {
        return count("userId", userId) > 0;
    }
}
