package com.example.orders.inventory.exception;

public class InventoryRejectedException extends RuntimeException {

    private final int status;

    public InventoryRejectedException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
