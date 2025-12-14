package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.model.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private String skuCode;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Integer restockThreshold;
    private Boolean isLowStock;
    private Boolean needsRestock;
    private LocalDateTime lastRestockedDate;
    private LocalDateTime nextRestockDate;
    private BigDecimal unitCost;
    private BigDecimal totalValue;
    private String locationCode;
    private String binLocation;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InventoryResponse fromEntity(InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .skuCode(item.getSkuCode())
                .quantity(item.getQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getAvailableQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .restockThreshold(item.getRestockThreshold())
                .isLowStock(item.isLowStock())
                .needsRestock(item.needsRestock())
                .lastRestockedDate(item.getLastRestockedDate())
                .nextRestockDate(item.getNextRestockDate())
                .unitCost(item.getUnitCost())
                .totalValue(item.getTotalValue())
                .locationCode(item.getLocationCode())
                .binLocation(item.getBinLocation())
                .isActive(item.getIsActive())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
