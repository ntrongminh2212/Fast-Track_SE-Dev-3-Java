package com.example.orders.inventory.client.config;

import com.example.orders.inventory.exception.InventoryRejectedException;
import com.example.orders.inventory.exception.InventoryTransientException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Feign-specific configuration; intentionally excluded from component scanning.
 */
public class InventoryFeignConfiguration {

    @Bean
    ErrorDecoder inventoryErrorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            if (response.status() == 503) {
                return new InventoryTransientException("Inventory temporarily unavailable (503)");
            }
            if (response.status() >= 400 && response.status() < 500) {
                return new InventoryRejectedException(response.status(),
                        "Inventory rejected the request (" + response.status() + ")");
            }
            return defaultDecoder.decode(methodKey, response);
        };
    }
}
