package com.ecommerce.order.dto;

import com.ecommerce.order.dto.orderitem.OrderItemRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Billing address is required")
    private String billingAddress;

    @NotNull(message = "Subtotal is required")
    private BigDecimal subtotal;

    @NotNull(message = "Tax is required")
    private BigDecimal tax;

    @NotNull(message = "Shipping fee is required")
    private BigDecimal shippingFee;

    @NotNull(message = "Total is required")
    private BigDecimal total;

    private String notes;

    @NotEmpty(message = "Order items cannot be empty")
    private List<@Valid OrderItemRequest> items;
}
