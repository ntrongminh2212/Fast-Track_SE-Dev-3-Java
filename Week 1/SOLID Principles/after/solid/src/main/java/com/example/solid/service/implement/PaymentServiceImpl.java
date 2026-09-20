package com.example.solid.service.implement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.springframework.stereotype.Service;

import com.example.solid.entity.Payment;
import com.example.solid.logger.PaymentLogger;
import com.example.solid.notification.NotificationService;
import com.example.solid.payment.PaymentProcessor;
import com.example.solid.payment.PaymentProcessorFactory;
import com.example.solid.repository.PaymentRepository;
import com.example.solid.request.OrderRequest;
import com.example.solid.service.PaymentService;
import com.example.solid.validator.OrderValidator;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final OrderValidator orderValidator;
    private final PaymentProcessorFactory paymentProcessorFactory;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final PaymentLogger paymentLogger;

    public PaymentServiceImpl(
            OrderValidator orderValidator,
            PaymentProcessorFactory paymentProcessorFactory,
            PaymentRepository paymentRepository,
            NotificationService notificationService,
            PaymentLogger paymentLogger) {
        this.orderValidator = orderValidator;
        this.paymentProcessorFactory = paymentProcessorFactory;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
        this.paymentLogger = paymentLogger;
    }

    @Override
    public void processPayment(OrderRequest order, String paymentMethod) throws Exception {
        // Validate
        orderValidator.validate(order);
        // Process payment
        double amount = order.getTotal();
        PaymentProcessor processor = paymentProcessorFactory.getPaymentProcessor(paymentMethod);
        processor.process(amount);
        // Save to database
        paymentRepository.save(new Payment(amount, paymentMethod));
        // Send email
        String message = "Payment of " + amount + " processed";
        String email = "customer@example.com";
        notificationService.send(email, message);
        // Log
        paymentLogger.logPaymentProcessed(order.getId());
    }
}