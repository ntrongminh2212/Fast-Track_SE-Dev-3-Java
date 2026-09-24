package com.example.orderservice.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * CreateOrderItemRequest
 */
@Data
public class CreateOrderItemRequest {
    @NotBlank
    private final String productId;
    @Min(1)
    private final int quantity;
    @Min(0)
    private final double price;
}
