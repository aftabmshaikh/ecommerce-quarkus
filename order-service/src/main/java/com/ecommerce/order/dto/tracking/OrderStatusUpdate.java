package com.ecommerce.order.dto.tracking;

import java.time.LocalDateTime;

public class OrderStatusUpdate {
    private String status;
    private String description;
    private String location;
    private LocalDateTime timestamp;
    private boolean isCurrent;
    
    // Constructors
    public OrderStatusUpdate() {}
    
    public OrderStatusUpdate(String status, String description, String location, 
                            LocalDateTime timestamp, boolean isCurrent) {
        this.status = status;
        this.description = description;
        this.location = location;
        this.timestamp = timestamp;
        this.isCurrent = isCurrent;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
}

