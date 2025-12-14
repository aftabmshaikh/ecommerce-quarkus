package com.ecommerce.inventory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "product_id", nullable = false, unique = true)
    private UUID productId;
    
    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold;
    
    @Column(name = "restock_threshold", nullable = false)
    private Integer restockThreshold;
    
    @Column(name = "last_restocked_date")
    private LocalDateTime lastRestockedDate;
    
    @Column(name = "next_restock_date")
    private LocalDateTime nextRestockDate;
    
    @Column(name = "unit_cost", precision = 19, scale = 4)
    private BigDecimal unitCost;
    
    @Column(name = "total_value", precision = 19, scale = 4)
    private BigDecimal totalValue;
    
    @Column(name = "location_code")
    private String locationCode;
    
    @Column(name = "bin_location")
    private String binLocation;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Version
    private Long version;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    public void calculateAvailableQuantity() {
        if (quantity == null) {
            quantity = 0;
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        this.availableQuantity = Math.max(0, quantity - reservedQuantity);
        
        if (unitCost != null) {
            this.totalValue = unitCost.multiply(BigDecimal.valueOf(quantity));
        }
        
        if (isActive == null) {
            this.isActive = true;
        }
        
        if (lowStockThreshold == null) {
            this.lowStockThreshold = 10; // Default threshold
        }
        
        if (restockThreshold == null) {
            this.restockThreshold = 20; // Default threshold
        }
    }
    
    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }
    
    public boolean needsRestock() {
        return availableQuantity <= restockThreshold;
    }
    
    public boolean canFulfill(int requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }
    
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Reserve quantity must be positive");
        }
        if (!canFulfill(quantity)) {
            throw new IllegalStateException("Insufficient available quantity");
        }
        this.reservedQuantity += quantity;
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }
    
    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        if (quantity > this.reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }
    
    public void adjustInventory(int adjustment) {
        if (this.quantity + adjustment < 0) {
            throw new IllegalStateException("Insufficient quantity for adjustment");
        }
        this.quantity += adjustment;
        this.availableQuantity = this.quantity - this.reservedQuantity;
    }
}
