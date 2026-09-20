package com.example.orderservice.domain.model;

import com.example.orderservice.domain.exception.DomainException;

public class OrderItem {
    private final String productId;
    private final int quantity;
    private final Money price;

    public OrderItem(String productId, int quantity, double price) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be positive");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.price = new Money(price);
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubtotal() {
        return new Money(price.getAmount() * quantity);
    }
}