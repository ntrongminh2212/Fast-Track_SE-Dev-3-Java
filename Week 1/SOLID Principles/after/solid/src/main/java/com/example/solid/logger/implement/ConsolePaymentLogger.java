package com.example.solid.logger.implement;

import com.example.solid.logger.PaymentLogger;

public class ConsolePaymentLogger implements PaymentLogger {

    @Override
    public void logPaymentProcessed(Long orderId) {
        System.out.println("Payment processed for order " + orderId);
    }

}
