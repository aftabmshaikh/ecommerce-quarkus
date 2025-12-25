package com.ecommerce.order.dto.returns;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class ReturnRequest {
    @NotEmpty(message = "At least one item must be returned")
    private List<ReturnItemRequest> items;
    
    @NotNull(message = "Return reason is required")
    private String reason;
    
    private String comments;
    
    // Getters and Setters
    public List<ReturnItemRequest> getItems() { return items; }
    public void setItems(List<ReturnItemRequest> items) { this.items = items; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    // Inner class for ReturnItemRequest
    public static class ReturnItemRequest {
        @NotNull(message = "Order item ID is required")
        private UUID orderItemId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        @NotNull(message = "Return reason is required")
        private String reason;
        
        // Getters and Setters
        public UUID getOrderItemId() { return orderItemId; }
        public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}

