package com.example.orders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

import com.example.orders.checkout.api.CheckoutRequest;
import com.example.orders.checkout.api.ReservationResult;
import com.example.orders.inventory.service.InventoryReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final InventoryReservationService inventoryReservationService;

    public OrderController(InventoryReservationService inventoryReservationService) {
        this.inventoryReservationService = inventoryReservationService;
    }

    @GetMapping({ "/api/orders", "/api/orders/{orderId}" })
    public Map<String, Object> getOrder(@PathVariable(required = false) String orderId) {
        return Map.of(
                "service", "order-service",
                "orderId", orderId == null ? "order-1001" : orderId,
                "status", "PENDING",
                "total", 49.99);
    }

    @PostMapping("/api/orders")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam(value = "simulate", required = false) String simulate) {
        String stableIdempotencyKey = idempotencyKey == null || idempotencyKey.isBlank()
                ? UUID.randomUUID().toString()
                : idempotencyKey;

        return inventoryReservationService.reserve(request, stableIdempotencyKey, correlationId, simulate)
                .thenApply(result -> toResponse(result, stableIdempotencyKey));
    }

    private ResponseEntity<Map<String, Object>> toResponse(ReservationResult result, String idempotencyKey) {
        HttpStatus status = HttpStatus.valueOf(result.httpStatus());
        return ResponseEntity.status(status).body(Map.of(
                "service", "order-service",
                "orderId", UUID.randomUUID().toString(),
                "status", result.reserved() ? "CREATED" : "NOT_CREATED",
                "sku", result.sku(),
                "quantity", result.quantity(),
                "inventoryReserved", result.reserved(),
                "retryCount", result.retryCount(),
                "circuitBreakerState", result.circuitBreakerState(),
                "idempotencyKey", idempotencyKey,
                "message", result.message()));
    }
}
