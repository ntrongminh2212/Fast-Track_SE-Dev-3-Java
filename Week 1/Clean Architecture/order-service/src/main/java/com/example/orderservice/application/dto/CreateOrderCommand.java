package com.example.orderservice.application.dto;

import java.util.List;

public class CreateOrderCommand {
    private final List<CreateOrderItemCommand> items;

    public CreateOrderCommand(List<CreateOrderItemCommand> items) {
        this.items = items;
    }

    public List<CreateOrderItemCommand> getItems() {
        return items;
    }
}
