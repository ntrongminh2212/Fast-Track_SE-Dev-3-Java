package com.example.orderservice.application.port.out;

import java.util.Optional;

import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderId;

public interface OrderRepository {
    void save(Order order);

    Optional<Order> findById(OrderId orderId);
}