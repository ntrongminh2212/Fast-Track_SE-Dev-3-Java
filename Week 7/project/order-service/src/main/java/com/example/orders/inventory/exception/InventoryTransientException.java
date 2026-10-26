package com.example.orders.inventory.exception;

public class InventoryTransientException extends RuntimeException {

    public InventoryTransientException(String message) {
        super(message);
    }
}
