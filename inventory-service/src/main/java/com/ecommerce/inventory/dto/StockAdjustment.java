package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustment {
    @NotBlank(message = "SKU code is required")
    private String skuCode;
    
    @NotNull(message = "Adjustment quantity is required")
    private Integer adjustment;
    
    private String reason;
    private String referenceId;
}
