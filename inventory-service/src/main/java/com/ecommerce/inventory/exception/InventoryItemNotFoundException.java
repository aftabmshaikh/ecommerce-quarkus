package com.ecommerce.inventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(String message) {
        super(message);
    }

    public InventoryItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
