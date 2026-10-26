package com.example.orders.inventory.client;

import com.example.orders.inventory.client.config.InventoryFeignConfiguration;
import com.example.orders.inventory.client.dto.InventoryReservationRequest;
import com.example.orders.inventory.client.dto.InventoryReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", url = "${INVENTORY_SERVICE_URL:http://localhost:8082}", configuration = InventoryFeignConfiguration.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/reservations")
    InventoryReservationResponse reserve(
            @RequestBody InventoryReservationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
            @RequestParam(value = "simulate", required = false) String simulate);
}
