package com.example.solid.service;

import com.example.solid.request.OrderRequest;

public interface PaymentService {
    void processPayment(OrderRequest order, String paymentMethod) throws Exception;
}
