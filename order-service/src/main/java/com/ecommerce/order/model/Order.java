package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"items", "statusHistory"})
@EqualsAndHashCode(exclude = {"items", "statusHistory"})
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(nullable = false)
    private UUID customerId;
    
    @Column(nullable = false, unique = true)
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private String shippingAddress;
    
    @Column(nullable = false)
    private String billingAddress;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "processing_at")
    private LocalDateTime processingAt;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "out_for_delivery_at")
    private LocalDateTime outForDeliveryAt;
    
    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;
    
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    private String carrier;
    
    @Version
    private Long version = 0L;
    
    private String currency;

    // Business methods
    public void addStatusHistory(OrderStatus status, String message) {
        if (this.statusHistory == null) {
            this.statusHistory = new ArrayList<>();
        }
        OrderStatusHistory history = OrderStatusHistory.create(status, message);
        history.setOrder(this);
        this.statusHistory.add(history);
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (this.orderNumber == null) {
            this.orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Status update methods
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        
        // Update relevant timestamps based on status
        if (status == OrderStatus.PAID) {
            this.paidAt = LocalDateTime.now();
        } else if (status == OrderStatus.PROCESSING) {
            this.processingAt = LocalDateTime.now();
        } else if (status == OrderStatus.SHIPPED) {
            this.shippedAt = LocalDateTime.now();
        } else if (status == OrderStatus.DELIVERED) {
            this.deliveredDate = LocalDateTime.now();
        } else if (status == OrderStatus.CANCELLED) {
            this.cancelledAt = LocalDateTime.now();
        } else if (status == OrderStatus.RETURN_REQUESTED) {
            this.returnRequestedAt = LocalDateTime.now();
        } else if (status == OrderStatus.RETURNED) {
            this.returnedAt = LocalDateTime.now();
        }
        
        // Add to status history
        if (this.statusHistory == null) {
            this.statusHistory = new ArrayList<>();
        }
        this.statusHistory.add(OrderStatusHistory.create(status, "Status updated to " + status));
    }

    // Helper methods
    public LocalDateTime getDeliveredAt() {
        return deliveredDate;
    }
    
    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredDate = deliveredAt;
    }
    
    @Transient
    public String getPaymentMethod() {
        // In a real implementation, you would fetch the most recent transaction
        // and return its payment method. For now, returning a default value.
        return "CREDIT_CARD"; // Default value, should be replaced with actual implementation
    }

    // Status check methods
    public boolean isPaid() {
        return status == OrderStatus.PAID ||
               status == OrderStatus.PROCESSING ||
               status == OrderStatus.PROCESSED ||
               status == OrderStatus.SHIPPED ||
               status == OrderStatus.OUT_FOR_DELIVERY ||
               status == OrderStatus.DELIVERED;
    }

    /**
     * Returns the total amount of the order including tax and shipping fee.
     * @return the total amount as BigDecimal
     */
    public BigDecimal getTotalAmount() {
        return total != null ? total : BigDecimal.ZERO;
    }

    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED || 
               status == OrderStatus.CANCELLED ||
               status == OrderStatus.RETURNED ||
               status == OrderStatus.REFUNDED;
    }

    // Item management methods
    public void addItem(OrderItem item) {
        if (item != null) {
            items.add(item);
            item.setOrder(this);
            calculateTotals();
        }
    }

    public void removeItem(OrderItem item) {
        if (item != null) {
            items.remove(item);
            item.setOrder(null);
            calculateTotals();
        }
    }

    public void calculateTotals() {
        if (this.items != null && !this.items.isEmpty()) {
            this.subtotal = this.items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate tax (10% of subtotal)
            this.tax = this.subtotal.multiply(new BigDecimal("0.10"));

            // Calculate shipping ($10 flat rate)
            this.shippingFee = new BigDecimal("10.00");

            // Calculate total
            this.total = this.subtotal.add(this.tax).add(this.shippingFee);
        } else {
            this.subtotal = BigDecimal.ZERO;
            this.tax = BigDecimal.ZERO;
            this.shippingFee = BigDecimal.ZERO;
            this.total = BigDecimal.ZERO;
        }
    }

    // Status update helper methods
    public void markAsPaid() {
        setStatus(OrderStatus.PAID);
        this.paidAt = LocalDateTime.now();
    }

    public void markAsProcessing() {
        setStatus(OrderStatus.PROCESSING);
        this.processingAt = LocalDateTime.now();
    }

    public void markAsShipped(String trackingNumber, String carrier) {
        this.trackingNumber = trackingNumber;
        this.carrier = carrier;
        setStatus(OrderStatus.SHIPPED);
        this.shippedAt = LocalDateTime.now();
    }

    public void markAsDelivered() {
        setStatus(OrderStatus.DELIVERED);
        this.deliveredDate = LocalDateTime.now();
    }

    public void markAsCancelled() {
        setStatus(OrderStatus.CANCELLED);
        this.cancelledAt = LocalDateTime.now();
    }
}
