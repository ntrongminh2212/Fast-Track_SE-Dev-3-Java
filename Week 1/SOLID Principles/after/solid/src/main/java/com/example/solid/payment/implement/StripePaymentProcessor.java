package com.example.solid.payment.implement;

import org.springframework.stereotype.Component;

import com.example.solid.payment.PaymentProcessor;

@Component
public class StripePaymentProcessor implements PaymentProcessor {

    @Override
    public void process(double amount) {
        // Call Stripe API
        System.out.println("Call Stripe API...");
        System.out.println("Successfully Charged: " + amount + "$");
    }

}
