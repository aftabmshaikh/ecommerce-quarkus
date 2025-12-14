package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.InvalidOrderException;
import com.ecommerce.order.exception.ResourceNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderService {

    private static final Logger log = Logger.getLogger(OrderService.class);

    @Inject
    OrderRepository orderRepository;

    @Inject
    @RestClient
    ProductServiceClient productServiceClient;

    @Inject
    OrderMapper orderMapper;

    @Inject
    @Channel("order-events")
    Emitter<Map<String, Object>> orderEventEmitter;

    @Transactional
    @Retry(maxRetries = 3, delay = 1000)
    public OrderResponse createOrder(OrderRequest request) {
        if (request == null || request.getCustomerId() == null) {
            throw new InvalidOrderException("Order request and customer ID cannot be null");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        checkProductAvailability(request);

        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        order.addStatusHistory(OrderStatus.PENDING, "Order created");

        BigDecimal total = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        orderRepository.persist(order);

        updateProductInventory(order);
        publishOrderEvent(order, "ORDER_CREATED");

        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrderById(UUID orderId) {
        log.infof("Fetching order with id: %s", orderId);
        Order order = orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrderByNumber(String orderNumber) {
        log.infof("Fetching order with number: %s", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getCustomerOrders(UUID customerId, int pageIndex, int pageSize) {
        log.infof("Fetching orders for customer: %s", customerId);
        return orderRepository.findByCustomerId(customerId, Page.of(pageIndex, pageSize)).stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        log.infof("Updating order %s status to %s", orderId, status);
        Order order = orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());

        order.addStatusHistory(status, String.format("Status changed from %s to %s", oldStatus, status));

        publishOrderEvent(order, "ORDER_STATUS_UPDATED");

        return orderMapper.toResponse(order);
    }

    @Transactional
    @Retry(maxRetries = 3, delay = 100)
    public OrderResponse cancelOrder(UUID orderId) {
        log.infof("Cancelling order: %s", orderId);
        Order order = orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getStatus().isCancellable()) {
            throw new InvalidOrderException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        order.addStatusHistory(OrderStatus.CANCELLED, "Order cancelled by customer");

        updateProductInventory(order, true);

        publishOrderEvent(order, "ORDER_CANCELLED");

        return orderMapper.toResponse(order);
    }

    @Retry(maxRetries = 3, delay = 1000)
    protected void checkProductAvailability(OrderRequest request) {
        log.info("Checking product availability for order");
        try {
            List<Map<String, Object>> itemsToCheck = request.getItems().stream()
                    .map(item -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", item.getProductId().toString());
                        map.put("quantity", item.getQuantity());
                        return map;
                    })
                    .collect(Collectors.toList());

            productServiceClient.checkStockAvailability(itemsToCheck);
        } catch (Exception e) {
            log.error("Error checking product availability", e);
            throw new RuntimeException("Error checking product availability", e);
        }
    }

    @Retry(maxRetries = 3, delay = 1000)
    protected void updateProductInventory(Order order) {
        updateProductInventory(order, false);
    }

    @Retry(maxRetries = 3, delay = 1000)
    protected void updateProductInventory(Order order, boolean isCancellation) {
        log.infof("Updating product inventory for order: %s", order.getId());
        try {
            List<Map<String, Object>> items = order.getItems().stream()
                    .map(item -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", item.getProductId().toString());
                        map.put("quantity", isCancellation ? item.getQuantity() : -item.getQuantity());
                        return map;
                    })
                    .collect(Collectors.toList());

            productServiceClient.updateInventory(items);
        } catch (Exception e) {
            log.error("Error updating product inventory", e);
            throw new RuntimeException("Error updating product inventory", e);
        }
    }

    protected void publishOrderEvent(Order order, String eventType) {
        log.infof("Publishing %s event for order: %s", eventType, order.getId());
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "orderId", order.getId().toString(),
                    "orderNumber", order.getOrderNumber(),
                    "customerId", order.getCustomerId().toString(),
                    "status", order.getStatus().name(),
                    "timestamp", LocalDateTime.now().toString()
            );

            orderEventEmitter.send(event);
        } catch (Exception e) {
            log.error("Error publishing order event", e);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
