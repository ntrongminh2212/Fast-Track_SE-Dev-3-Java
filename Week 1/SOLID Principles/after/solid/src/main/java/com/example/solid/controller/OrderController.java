package com.example.solid.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.solid.request.OrderRequest;
import com.example.solid.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final PaymentService paymentService;

    public OrderController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public ResponseEntity<String> processPayment(
            @Valid @RequestBody OrderRequest order,
            @RequestParam String paymentMethod) throws Exception {

        paymentService.processPayment(order, paymentMethod);

        return ResponseEntity.ok("Payment processed successfully");
    }
}