package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatus {
    private String skuCode;
    private boolean inStock;
    private Integer availableQuantity;
    private boolean lowStock;
    private String status;
}
