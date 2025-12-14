package com.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    private UUID userId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String couponCode;
    private BigDecimal discount;
}
