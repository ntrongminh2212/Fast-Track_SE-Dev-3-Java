package com.example.orderservice.domain.model;

import com.example.orderservice.domain.exception.DomainException;

public record OrderId(String id) {
    public OrderId(String id) {
        if (id == null || id.isEmpty()) {
            throw new DomainException("OrderId cannot be empty");
        }
        this.id = id;
    }
}