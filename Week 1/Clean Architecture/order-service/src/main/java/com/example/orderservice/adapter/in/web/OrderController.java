package com.example.orderservice.adapter.in.web;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.adapter.in.web.dto.CreateOrderRequest;
import com.example.orderservice.application.dto.CreateOrderCommand;
import com.example.orderservice.application.dto.CreateOrderItemCommand;
import com.example.orderservice.application.dto.GetOrderResponse;
import com.example.orderservice.application.dto.OrderResponse;
import com.example.orderservice.application.exception.ApplicationException;
import com.example.orderservice.application.port.in.CreateOrderUseCase;
import com.example.orderservice.application.port.in.GetOrderUseCase;
import com.example.orderservice.domain.exception.DomainException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, GetOrderUseCase getOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody CreateOrderRequest request) {
        try {
            // Convert HTTP request to command
            CreateOrderCommand command = new CreateOrderCommand(
                    request.getItems().stream()
                            .map(itemReq -> new CreateOrderItemCommand(
                                    itemReq.getProductId(),
                                    itemReq.getQuantity(),
                                    itemReq.getPrice()))
                            .collect(Collectors.toList()));

            // Execute use case
            OrderResponse response = createOrderUseCase.execute(command);

            // Return response
            return ResponseEntity.ok(response);
        } catch (ApplicationException e) {
            return ResponseEntity.badRequest().build();
        } catch (DomainException e) {
            return ResponseEntity.status(400).build();
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrderbyId(
            @PathVariable String orderId) {
        GetOrderResponse response = getOrderUseCase.getOrderbyId(orderId);
        return ResponseEntity.ok(response);
    }
}