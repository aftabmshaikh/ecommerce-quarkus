package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@EqualsAndHashCode(exclude = "order")
public class OrderItem {
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(nullable = false)
    private UUID productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private String productSku;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status = OrderItemStatus.CREATED;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version = 0L;
    
    @Column(name = "reviewed", nullable = false)
    private boolean reviewed = false;
    
    @Column(name = "returned_quantity", nullable = false)
    private int returnedQuantity = 0;
    
    @Transient
    private BigDecimal totalPrice;
    
    /**
     * Calculates and updates the total price based on unit price, quantity, discount, and tax.
     */
    private void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            this.totalPrice = subtotal.subtract(discount).add(tax);
            if (this.totalPrice.compareTo(BigDecimal.ZERO) < 0) {
                this.totalPrice = BigDecimal.ZERO;
            }
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Updates the unit price and recalculates the total price.
     * @param unitPrice the new unit price
     * @throws IllegalArgumentException if unitPrice is null or negative
     */
    public void updateUnitPrice(BigDecimal unitPrice) {
        setUnitPrice(unitPrice);
    }
    
    /**
     * Updates the quantity and recalculates the total price.
     * @param quantity the new quantity
     * @throws IllegalArgumentException if quantity is null or negative
     */
    public void updateQuantity(Integer quantity) {
        setQuantity(quantity);
    }
    
    @PrePersist
    @PreUpdate
    protected void onPersistOrUpdate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        calculateTotalPrice();
    }
    
}
