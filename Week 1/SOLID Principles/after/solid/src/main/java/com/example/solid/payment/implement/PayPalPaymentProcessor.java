package com.example.solid.payment.implement;

import com.example.solid.payment.PaymentProcessor;

public class PayPalPaymentProcessor implements PaymentProcessor {

    @Override
    public void process(double amount) {
        // Call PayPal API
        System.out.println("Call PayPal API...");
        System.out.println("Successfully Paid: " + amount + "$");
    }

}
