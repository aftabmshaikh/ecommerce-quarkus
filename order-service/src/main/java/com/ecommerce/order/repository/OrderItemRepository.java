package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OrderItemRepository implements PanacheRepository<OrderItem> {

    public List<OrderItem> findByOrder(Order order) {
        return list("order", order);
    }

    public List<OrderItem> findByProductId(UUID productId) {
        return list("productId", productId);
    }

    public boolean existsByProductId(UUID productId) {
        return count("productId", productId) > 0;
    }
}
