package com.ecommerce.order.dto.shipping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShippingOption {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String estimatedDelivery;
    private boolean isRecommended;
    private String carrier;
    private String serviceLevel;
    private LocalDateTime minDeliveryDate;
    private LocalDateTime maxDeliveryDate;
    private boolean hasTracking;
    private boolean hasInsurance;
    private boolean hasSignatureConfirmation;
    private boolean supportsCod;
    private Double maxWeight;
    private Double maxLength;
    private Double maxWidth;
    private Double maxHeight;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    
    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }
    
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    
    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
    
    public LocalDateTime getMinDeliveryDate() { return minDeliveryDate; }
    public void setMinDeliveryDate(LocalDateTime minDeliveryDate) { this.minDeliveryDate = minDeliveryDate; }
    
    public LocalDateTime getMaxDeliveryDate() { return maxDeliveryDate; }
    public void setMaxDeliveryDate(LocalDateTime maxDeliveryDate) { this.maxDeliveryDate = maxDeliveryDate; }
    
    public boolean isHasTracking() { return hasTracking; }
    public void setHasTracking(boolean hasTracking) { this.hasTracking = hasTracking; }
    
    public boolean isHasInsurance() { return hasInsurance; }
    public void setHasInsurance(boolean hasInsurance) { this.hasInsurance = hasInsurance; }
    
    public boolean isHasSignatureConfirmation() { return hasSignatureConfirmation; }
    public void setHasSignatureConfirmation(boolean hasSignatureConfirmation) { this.hasSignatureConfirmation = hasSignatureConfirmation; }
    
    public boolean isSupportsCod() { return supportsCod; }
    public void setSupportsCod(boolean supportsCod) { this.supportsCod = supportsCod; }
    
    public Double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }
    
    public Double getMaxLength() { return maxLength; }
    public void setMaxLength(Double maxLength) { this.maxLength = maxLength; }
    
    public Double getMaxWidth() { return maxWidth; }
    public void setMaxWidth(Double maxWidth) { this.maxWidth = maxWidth; }
    
    public Double getMaxHeight() { return maxHeight; }
    public void setMaxHeight(Double maxHeight) { this.maxHeight = maxHeight; }
}

