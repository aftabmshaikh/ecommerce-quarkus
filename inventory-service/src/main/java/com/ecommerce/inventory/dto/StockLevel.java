package com.ecommerce.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLevel {
    private String skuCode;
    private Integer currentLevel;
    private Integer lowStockThreshold;
    private Integer restockThreshold;
    private String status;
}
