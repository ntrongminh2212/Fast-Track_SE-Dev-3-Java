package com.example.solid.validator;

import com.example.solid.request.OrderRequest;

public class OrderValidator {
    public void validate(OrderRequest order) throws Exception {
        if (order == null) {
            throw new Exception("Invalid order");
        }
    }
}