package com.ecommerce.cart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "product_image")
    private String productImage;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private int quantity;
    
    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;
    
    // Additional fields for product options can be added here
    
    @PrePersist
    @PreUpdate
    public void calculateItemTotal() {
        if (unitPrice != null) {
            this.itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
