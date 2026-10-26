package com.example.orders.inventory.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

import com.example.orders.checkout.api.CheckoutRequest;
import com.example.orders.checkout.api.ReservationResult;
import com.example.orders.inventory.client.InventoryClient;
import com.example.orders.inventory.client.dto.InventoryReservationRequest;
import com.example.orders.inventory.client.dto.InventoryReservationResponse;
import com.example.orders.inventory.exception.InventoryRejectedException;
import com.example.orders.inventory.exception.InventoryTransientException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class InventoryReservationService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryReservationService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final String CIRCUIT_BREAKER_NAME = "inventoryReservation";

    private final InventoryClient inventoryClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ThreadPoolExecutor inventoryExecutor;

    public InventoryReservationService(
            InventoryClient inventoryClient,
            CircuitBreakerRegistry circuitBreakerRegistry,
            @Qualifier("inventoryReservationExecutor") ThreadPoolExecutor inventoryExecutor) {
        this.inventoryClient = inventoryClient;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.inventoryExecutor = inventoryExecutor;
    }

    public CompletableFuture<ReservationResult> reserve(
            CheckoutRequest request, String idempotencyKey, String correlationId, String simulate) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
                int[] retries = { 0 };

                try {
                    ReservationResult result = circuitBreaker.executeSupplier(
                            () -> reserveWithRetry(request, idempotencyKey, correlationId, simulate, retries));
                    return new ReservationResult(result.reserved(), result.sku(), result.quantity(),
                            result.retryCount(),
                            result.httpStatus(), circuitBreaker.getState().name(), result.message());
                } catch (InventoryRejectedException exception) {
                    logger.info("Inventory rejected checkout with status={}", exception.status());
                    return new ReservationResult(false, request.sku(), request.quantity(), retries[0],
                            exception.status(), circuitBreaker.getState().name(), exception.getMessage());
                } catch (RuntimeException exception) {
                    logger.warn("Inventory reservation failed; circuitBreakerState={}, retries={}, reason={}",
                            circuitBreaker.getState(), retries[0], exception.toString());
                    return new ReservationResult(false, request.sku(), request.quantity(), retries[0], 503,
                            circuitBreaker.getState().name(), "Inventory temporarily unavailable");
                }
            }, inventoryExecutor);
        } catch (RejectedExecutionException exception) {
            logger.warn("Inventory reservation bulkhead is full");
            return CompletableFuture.completedFuture(new ReservationResult(
                    false, request.sku(), request.quantity(), 0, 503, "BULKHEAD_FULL",
                    "Inventory reservation capacity is full"));
        }
    }

    private ReservationResult reserveWithRetry(
            CheckoutRequest request,
            String idempotencyKey,
            String correlationId,
            String simulate,
            int[] retries) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                InventoryReservationResponse response = inventoryClient.reserve(
                        new InventoryReservationRequest(request.sku(), request.quantity()),
                        idempotencyKey,
                        correlationId,
                        simulate);
                return new ReservationResult(response.reserved(), response.sku(), response.quantity(),
                        retries[0], response.reserved() ? 201 : 409, "CLOSED",
                        response.reserved() ? "Stock reserved" : "Stock unavailable");
            } catch (RetryableException | InventoryTransientException exception) {
                if (attempt == MAX_ATTEMPTS) {
                    throw exception;
                }

                retries[0]++;
                long backoffMillis = (100L << (attempt - 1)) + ThreadLocalRandom.current().nextLong(50, 151);
                logger.warn("Transient inventory error; retry={}/{} backoffMs={}",
                        retries[0], MAX_ATTEMPTS - 1, backoffMillis);
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting to retry inventory",
                            interruptedException);
                }
            }
        }
        throw new IllegalStateException("Inventory retry loop ended unexpectedly");
    }
}
