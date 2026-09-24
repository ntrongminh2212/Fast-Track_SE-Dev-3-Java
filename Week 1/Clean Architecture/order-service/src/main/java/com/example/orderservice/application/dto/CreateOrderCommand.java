package com.example.orderservice.application.dto;

import java.util.List;

public class CreateOrderCommand {
    private final String orderId;
    private final List<CreateOrderItemCommand> items;

    public CreateOrderCommand(String orderId, List<CreateOrderItemCommand> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<CreateOrderItemCommand> getItems() {
        return items;
    }
}
