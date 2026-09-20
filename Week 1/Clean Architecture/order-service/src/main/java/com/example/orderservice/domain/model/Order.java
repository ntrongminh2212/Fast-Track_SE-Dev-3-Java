package com.example.orderservice.domain.model;

import java.util.List;

public class Order {
    private final OrderId id;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(OrderId id, List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.id = id;
        this.items = items;
        this.status = OrderStatus.PENDING;
    }

    public double calculateTotal() {
        return items.stream()
                .mapToDouble(item -> item.getPrice().getAmount() * item.getQuantity())
                .sum();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm non-pending order");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}