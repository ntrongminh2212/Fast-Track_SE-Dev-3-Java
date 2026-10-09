package com.example.orderservice.application.service;

import java.util.List;
import java.util.Map;

import com.example.orderservice.application.dto.GetOrderItemsResponse;
import com.example.orderservice.application.dto.GetOrderResponse;
import com.example.orderservice.application.exception.ApplicationException;
import com.example.orderservice.application.port.in.GetOrderUseCase;
import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.application.port.out.ProductRepository;
import com.example.orderservice.domain.model.Order;
import com.example.orderservice.domain.model.OrderId;

public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public GetOrderService(OrderRepository orderRepository,
            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public GetOrderResponse getOrderbyId(String orderId) {
        Order order = orderRepository.findById(new OrderId(orderId))
                .orElseThrow(() -> new ApplicationException("Order id " + orderId + " not found"));

        Map<String, String> productNameByIdMap = productRepository.findNamesByIds(order.getItems().stream()
                .map(orderItem -> orderItem.getProductId())
                .toList());

        List<GetOrderItemsResponse> items = order.getItems().stream()
                .map(orderItem -> new GetOrderItemsResponse(
                        orderItem.getProductId(),
                        orderItem.getQuantity(),
                        orderItem.getPrice().getAmount(),
                        productNameByIdMap.getOrDefault(orderItem.getProductId(), "Not available")))
                .toList();

        return new GetOrderResponse(
                order.getId().getValue(),
                order.calculateTotal(),
                order.getStatus().name(),
                items);
    }

}
