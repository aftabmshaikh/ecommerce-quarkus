package com.ecommerce.order.dto.tracking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderTrackingResponse {
    private UUID orderId;
    private String orderNumber;
    private String status;
    private String trackingNumber;
    private String carrier;
    private String carrierUrl;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private List<OrderStatusUpdate> statusUpdates;
    
    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    public String getCarrierUrl() { return carrierUrl; }
    public void setCarrierUrl(String carrierUrl) { this.carrierUrl = carrierUrl; }
    
    public LocalDateTime getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(LocalDateTime estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public LocalDateTime getActualDelivery() { return actualDelivery; }
    public void setActualDelivery(LocalDateTime actualDelivery) { this.actualDelivery = actualDelivery; }
    
    public List<OrderStatusUpdate> getStatusUpdates() { return statusUpdates; }
    public void setStatusUpdates(List<OrderStatusUpdate> statusUpdates) { this.statusUpdates = statusUpdates; }
}

