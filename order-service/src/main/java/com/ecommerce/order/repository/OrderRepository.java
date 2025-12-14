package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<Order, UUID> {

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return find("orderNumber", orderNumber).firstResultOptional();
    }

    public List<Order> findByCustomerId(UUID customerId) {
        return list("customerId", customerId);
    }

    public List<Order> findByCustomerId(UUID customerId, Page page) {
        return find("customerId", customerId).page(page).list();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return list("status", status);
    }

    public List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return list("createdAt between ?1 and ?2", startDate, endDate);
    }

    public List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status, Page page) {
        return find("customerId = ?1 and status = ?2", customerId, status).page(page).list();
    }

    public List<Order> findByCustomerEmail(String email, Page page) {
        return find("customerEmail = ?1 order by createdAt desc", email).page(page).list();
    }

    public List<Order> findOrdersContainingProduct(UUID productId) {
        return list("select o from Order o join o.items i where i.productId = ?1", productId);
    }

    public boolean existsByIdAndCustomerId(UUID orderId, UUID customerId) {
        return count("id = ?1 and customerId = ?2", orderId, customerId) > 0;
    }
}
