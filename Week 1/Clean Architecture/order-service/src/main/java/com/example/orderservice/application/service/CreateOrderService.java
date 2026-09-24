package com.example.orderservice.application.service;

import java.util.List;
import java.util.stream.Collectors;

import com.example.orderservice.application.dto.CreateOrderCommand;
import com.example.orderservice.application.dto.OrderResponse;
import com.example.orderservice.application.port.in.CreateOrderUseCase;
import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.domain.model.Money;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderId;
import com.example.orderservice.domain.model.OrderItem;

public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderValidator validator;

    public CreateOrderService(OrderRepository orderRepository, OrderValidator validator) {
        this.orderRepository = orderRepository;
        this.validator = validator;
    }

    @Override
    public OrderResponse execute(CreateOrderCommand command) {
        // 1. Validate
        validator.validate(command);

        // 2. Create domain objects
        OrderId orderId = new OrderId(command.getOrderId());
        List<OrderItem> items;
        items = command.getItems().stream()
                .map(itemCmd -> new OrderItem(
                        itemCmd.getProductId(),
                        itemCmd.getQuantity(),
                        itemCmd.getPrice()))
                .collect(Collectors.toList());

        // 3. Create and manipulate aggregate
        Order order = new Order(orderId, items);
        order.confirm(); // business operation

        // 4. Persist
        orderRepository.save(order);

        // 5. Return response
        Money total = new Money(order.calculateTotal());
        return new OrderResponse(
                order.getId().getValue(),
                total.getAmount(),
                order.getStatus().toString());
    }

}