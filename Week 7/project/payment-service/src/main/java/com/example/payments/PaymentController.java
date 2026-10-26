package com.example.payments;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @GetMapping({ "/api/payments", "/api/payments/{paymentId}" })
    public Map<String, Object> getPayment(@PathVariable(required = false) String paymentId) {
        return Map.of(
                "service", "payment-service",
                "paymentId", paymentId == null ? "payment-1001" : paymentId,
                "status", "AUTHORIZED",
                "amount", 49.99);
    }
}
