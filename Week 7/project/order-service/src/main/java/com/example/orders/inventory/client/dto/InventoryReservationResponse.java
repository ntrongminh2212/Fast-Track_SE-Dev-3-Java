package com.example.orders.inventory.client.dto;

public record InventoryReservationResponse(String sku, int quantity, boolean reserved) {
}
