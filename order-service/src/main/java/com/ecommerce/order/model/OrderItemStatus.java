package com.ecommerce.order.model;

/**
 * Represents the status of an order item in the system.
 */
public enum OrderItemStatus {
    /**
     * Item has been created but not yet processed.
     */
    CREATED,
    
    /**
     * Item is being processed.
     */
    PROCESSING,
    
    /**
     * Item is ready to be shipped.
     */
    READY_FOR_SHIPMENT,
    
    /**
     * Item has been shipped.
     */
    SHIPPED,
    
    /**
     * Item has been delivered.
     */
    DELIVERED,
    
    /**
     * Item is backordered.
     */
    BACKORDERED,
    
    /**
     * Item is out of stock.
     */
    OUT_OF_STOCK,
    
    /**
     * Item has been cancelled.
     */
    CANCELLED,
    
    /**
     * Item has been returned.
     */
    RETURNED,
    
    /**
     * Item is being refunded.
     */
    REFUNDED,
    
    /**
     * Item is on hold.
     */
    ON_HOLD,
    
    /**
     * Item is awaiting payment.
     */
    AWAITING_PAYMENT,
    
    /**
     * Item is awaiting fulfillment.
     */
    AWAITING_FULFILLMENT,
    
    /**
     * Item is awaiting shipment.
     */
    AWAITING_SHIPMENT,
    
    /**
     * Item is awaiting pickup.
     */
    AWAITING_PICKUP,
    
    /**
     * Item is completed.
     */
    COMPLETED,
    
    /**
     * Item has failed processing.
     */
    FAILED
}
