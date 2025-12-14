package com.ecommerce.order.dto;

import com.ecommerce.order.dto.orderitem.OrderItemResponse;
import com.ecommerce.order.model.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerEmail;
    private String customerPhone;
    private String status;
    private String shippingAddress;
    private String billingAddress;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;

    public static OrderResponse fromEntity(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingFee(order.getShippingFee())
                .total(order.getTotal())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() != null ?
                        order.getItems().stream()
                                .map(OrderItemResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .statusHistory(order.getStatusHistory() != null && !order.getStatusHistory().isEmpty() ?
                        order.getStatusHistory().stream()
                                .map(OrderStatusHistoryResponse::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }
}
