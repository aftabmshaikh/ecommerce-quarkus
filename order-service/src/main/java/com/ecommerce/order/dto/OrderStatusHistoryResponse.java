package com.ecommerce.order.dto;

import com.ecommerce.order.model.OrderStatusHistory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusHistoryResponse {
    private String status;
    private String message;
    private LocalDateTime statusDate;

    public static OrderStatusHistoryResponse fromEntity(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }
        return OrderStatusHistoryResponse.builder()
                .status(history.getStatus() != null ? history.getStatus().name() : null)
                .message(history.getMessage())
                .statusDate(history.getStatusDate())
                .build();
    }
}
