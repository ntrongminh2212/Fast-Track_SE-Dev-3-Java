package com.example.inventory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    private final ConcurrentMap<String, InventoryReservationResponse> reservations = new ConcurrentHashMap<>();

    @GetMapping({ "/api/inventory", "/api/inventory/{sku}" })
    public Map<String, Object> getInventory(@PathVariable(required = false) String sku) {
        return Map.of(
                "service", "inventory-service",
                "sku", sku == null ? "SKU-1001" : sku,
                "available", true,
                "quantity", 24);
    }

    @PostMapping("/api/inventory/reservations")
    public InventoryReservationResponse reserve(
            @RequestBody InventoryReservationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam(value = "simulate", required = false) String simulate) {
        simulateFailure(simulate);
        if (request.sku() == null || request.sku().isBlank() || request.quantity() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sku and positive quantity are required");
        }

        return reservations.computeIfAbsent(idempotencyKey,
                key -> new InventoryReservationResponse(request.sku(), request.quantity(), request.quantity() <= 24));
    }

    private void simulateFailure(String simulate) {
        if ("slow".equalsIgnoreCase(simulate)) {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Simulation interrupted", exception);
            }
        } else if ("unavailable".equalsIgnoreCase(simulate)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Simulated transient failure");
        } else if ("server-error".equalsIgnoreCase(simulate)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Simulated server failure");
        } else if ("bad-request".equalsIgnoreCase(simulate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Simulated validation failure");
        }
    }

    public record InventoryReservationRequest(String sku, int quantity) {
    }

    public record InventoryReservationResponse(String sku, int quantity, boolean reserved) {
    }
}
