package com.example.orderservice.domain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.orderservice.domain.exception.DomainException;

@SpringBootTest
public class OrderTest {

    @Test
    public void testOrderMustHaveItems() {
        assertThrows(DomainException.class, () -> {
            new Order(new OrderId("123"), new ArrayList<>());
        });
    }

    @Test
    public void testCalculateTotal() {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("P1", 2, 100.0),
                new OrderItem("P2", 1, 50.0));
        Order order = new Order(new OrderId("123"), items);

        assertEquals(250.0, order.calculateTotal());
    }

    @Test
    public void testConfirmOrder() {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("P1", 1, 100.0));
        Order order = new Order(new OrderId("123"), items);

        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    public void testCannotConfirmTwice() {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("P1", 1, 100.0));
        Order order = new Order(new OrderId("123"), items);

        order.confirm();
        assertThrows(DomainException.class, order::confirm);
    }
}