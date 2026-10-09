package com.example.orderservice.application.port.in;

import com.example.orderservice.application.dto.GetOrderResponse;

public interface GetOrderUseCase {
    public GetOrderResponse getOrderbyId(String orderId);
}
