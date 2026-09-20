package com.example.orderservice.application.port.in;

public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderValidator validator;

    public CreateOrderUseCase(OrderRepository orderRepository, OrderValidator validator) {
        this.orderRepository = orderRepository;
        this.validator = validator;
    }

    public OrderResponse execute(CreateOrderCommand command) {
        // 1. Validate
        validator.validate(command);

        // 2. Create domain objects
        OrderId orderId = new OrderId(command.getOrderId());
        List<OrderItem> items = command.getItems().stream()
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
        Money total = order.calculateTotal();
        return new OrderResponse(
                order.getId().getValue(),
                total.getAmount(),
                order.getStatus().toString());
    }
}