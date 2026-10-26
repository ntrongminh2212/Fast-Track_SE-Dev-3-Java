package com.example.orders.checkout.api;

public record ReservationResult(
        boolean reserved,
        String sku,
        int quantity,
        int retryCount,
        int httpStatus,
        String circuitBreakerState,
        String message) {
}
