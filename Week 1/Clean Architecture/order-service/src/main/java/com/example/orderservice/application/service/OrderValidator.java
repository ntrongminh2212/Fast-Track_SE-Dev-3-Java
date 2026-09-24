package com.example.orderservice.application.service;

import com.example.orderservice.application.dto.CreateOrderCommand;
import com.example.orderservice.application.dto.CreateOrderItemCommand;
import com.example.orderservice.application.exception.ApplicationException;

public class OrderValidator {
    public void validate(CreateOrderCommand command) {
        if (command.getOrderId() == null || command.getOrderId().trim().isEmpty()) {
            throw new ApplicationException("OrderId is required");
        }

        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new ApplicationException("Order must have items");
        }

        for (CreateOrderItemCommand item : command.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new ApplicationException("Quantity must be positive");
            }
            if (item.getPrice() < 0) {
                throw new ApplicationException("Price cannot be negative");
            }
        }
    }
}
