package com.example.orderservice.domain.model;

import java.util.Objects;

import com.example.orderservice.domain.exception.DomainException;

public record Money(double amount) {

    public Money(double amount) {
        if (amount < 0) {
            throw new DomainException("Money cannot be negative");
        }
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }
}
