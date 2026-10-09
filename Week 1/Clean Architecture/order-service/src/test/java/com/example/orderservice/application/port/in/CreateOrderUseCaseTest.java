package com.example.orderservice.application.port.in;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.orderservice.adapter.out.persistent.InMemoryOrderRepository;
import com.example.orderservice.application.dto.CreateOrderCommand;
import com.example.orderservice.application.dto.CreateOrderItemCommand;
import com.example.orderservice.application.dto.OrderResponse;
import com.example.orderservice.application.exception.ApplicationException;
import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.application.service.CreateOrderService;
import com.example.orderservice.application.service.OrderValidator;

public class CreateOrderUseCaseTest {
    private OrderRepository orderRepository;
    private OrderValidator validator;
    private CreateOrderUseCase useCase;

    @BeforeEach
    public void setUp() {
        orderRepository = new InMemoryOrderRepository();
        validator = new OrderValidator();
        useCase = new CreateOrderService(orderRepository, validator);
    }

    @Test
    public void testCreateOrderSuccess() {
        CreateOrderCommand command = new CreateOrderCommand(
                Arrays.asList(
                        new CreateOrderItemCommand("P1", 2, 100.0)));

        OrderResponse response = useCase.execute(command);

        assertEquals("ORD123", response.getOrderId());
        assertEquals(200.0, response.getTotal());
        assertEquals("CONFIRMED", response.getStatus());
    }

    @Test
    public void testCreateOrderEmptyItems() {
        CreateOrderCommand command = new CreateOrderCommand(new ArrayList<>());

        assertThrows(ApplicationException.class, () -> useCase.execute(command));
    }
}
