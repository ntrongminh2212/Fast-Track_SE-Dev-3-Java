package com.example.orderservice.adapter.out.persistent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderId;

public class InMemoryOrderRepository implements OrderRepository {

    private Map<OrderId, Order> orderMemoryMap = new HashMap<>();

    @Override
    public void save(Order order) {
        orderMemoryMap.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return Optional.ofNullable(orderMemoryMap.get(orderId));
    }

    @Override
    public String nextOrderId() {
        // TODO Auto-generated method stub
        return Collections.max(orderMemoryMap.keySet(), Comparator.comparing(OrderId::id)).getValue();
    }

}
