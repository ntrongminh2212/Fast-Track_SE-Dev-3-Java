package com.example.solid.payment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class PaymentProcessorFactory {
    private final Map<String, PaymentProcessor> processors;

    public PaymentProcessorFactory(List<PaymentProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(p -> p.getClass().getName(), Function.identity()));
    }

    public PaymentProcessor getPaymentProcessor(String paymentMethod) throws Exception {
        PaymentProcessor processor = null;
        switch (paymentMethod) {
            case "CREDIT_CARD":
                processor = processors.get("StripePaymentProcessor");
                break;
            case "PAYPAL":
                processor = processors.get("PayPalPaymentProcessor");
                break;
            case "BANK":
                processor = processors.get("BankPaymentProcessor");
                break;
            default:
                throw new Exception("Unsupported payment method: " + paymentMethod);
        }
        return processor;
    }
}
