package com.ecommerce.order.model;

/**
 * Represents the status of an order in the system.
 */
public enum OrderStatus {
    /**
     * Order has been created but payment is pending.
     */
    PENDING,
    
    /**
     * Order has been placed by the customer.
     */
    PLACED,
    
    /**
     * Payment has been successfully processed.
     */
    PAID,
    
    /**
     * Payment has been received and order is being processed.
     */
    PROCESSING,
    
    /**
     * Order has been processed and is ready for shipping.
     */
    PROCESSED,
    
    /**
     * Order has been shipped to the customer.
     */
    SHIPPED,
    
    /**
     * Order is out for delivery.
     */
    OUT_FOR_DELIVERY,
    
    /**
     * Order has been delivered to the customer.
     */
    DELIVERED,
    
    /**
     * Order has been cancelled.
     */
    CANCELLED,
    
    /**
     * A return has been requested for this order.
     */
    RETURN_REQUESTED,
    
    /**
     * Order has been returned by the customer.
     */
    RETURNED,
    
    /**
     * Refund has been processed for the order.
     */
    REFUNDED,
    
    /**
     * Order processing has failed.
     */
    FAILED,
    
    /**
     * Order is on hold due to payment or other issues.
     */
    ON_HOLD,
    
    /**
     * Order is awaiting payment confirmation.
     */
    AWAITING_PAYMENT,
    
    /**
     * Order is awaiting fulfillment (e.g., waiting for stock).
     */
    AWAITING_FULFILLMENT,
    
    /**
     * Order is completed successfully.
     */
    COMPLETED,
    
    /**
     * Order is awaiting shipment.
     */
    AWAITING_SHIPMENT,
    
    /**
     * Order is awaiting pickup by the customer.
     */
    AWAITING_PICKUP,
    
    /**
     * Order has been partially shipped.
     */
    PARTIALLY_SHIPPED,
    
    /**
     * Order has been partially refunded.
     */
    PARTIALLY_REFUNDED,
    
    /**
     * Order has been declined.
     */
    DECLINED,
    
    /**
     * Order is in dispute.
     */
    DISPUTED,
    
    /**
     * Order requires manual verification.
     */
    VERIFICATION_REQUIRED;
    
    /**
     * Checks if the order status indicates that payment has been received.
     * @return true if the status indicates payment has been received, false otherwise
     */
    public boolean isPaid() {
        return this == PAID || 
               this == PROCESSING || 
               this == PROCESSED || 
               this == SHIPPED || 
               this == OUT_FOR_DELIVERY || 
               this == DELIVERED ||
               this == COMPLETED;
    }
    
    /**
     * Checks if the order can be cancelled in its current status.
     * @return true if the order can be cancelled, false otherwise
     */
    public boolean isCancellable() {
        return this == PENDING || this == PLACED || this == PAID || this == PROCESSING;
    }
}
