package com.ecommerce.order.dto.returns;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ReturnResponse {
    private UUID id;
    private String returnNumber;
    private UUID orderId;
    private String orderNumber;
    private String status;
    private String returnReason;
    private String comments;
    private BigDecimal refundAmount;
    private String refundMethod;
    private LocalDateTime requestedDate;
    private LocalDateTime processedDate;
    private List<ReturnItemResponse> items;
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getReturnNumber() { return returnNumber; }
    public void setReturnNumber(String returnNumber) { this.returnNumber = returnNumber; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
    
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    
    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }
    
    public List<ReturnItemResponse> getItems() { return items; }
    public void setItems(List<ReturnItemResponse> items) { this.items = items; }
    
    // Inner class for ReturnItemResponse
    public static class ReturnItemResponse {
        private UUID id;
        private UUID orderItemId;
        private UUID productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private java.math.BigDecimal unitPrice;
        private java.math.BigDecimal refundAmount;
        private String reason;
        private String status;
        
        // Getters and Setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        
        public UUID getOrderItemId() { return orderItemId; }
        public void setOrderItemId(UUID orderItemId) { this.orderItemId = orderItemId; }
        
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public java.math.BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(java.math.BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}

