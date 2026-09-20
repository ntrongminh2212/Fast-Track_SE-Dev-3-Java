package com.example.solid.payment.implement;

import org.springframework.stereotype.Component;

import com.example.solid.payment.PaymentProcessor;

@Component
public class BankPaymentProcessor implements PaymentProcessor {

    @Override
    public void process(double amount) {
        // Call Bank API
        System.out.println("Call Bank API...");
        System.out.println("Successfully Transfer: " + amount + "$");
    }

}
