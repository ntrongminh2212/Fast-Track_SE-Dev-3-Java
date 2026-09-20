package com.example.solid.notification.implement;

import org.springframework.stereotype.Component;

import com.example.solid.notification.NotificationService;

@Component
public class SmtpNotificationService implements NotificationService {

    @Override
    public void send(String email, String message) {
        // SMTP smtp = new SMTP("smtp.gmail.com", 587, "user@gmail.com", "password");
        // smtp.send(email, "Payment Confirmation", message);
        System.out.println("Send SMTP Payment Confirmation to " + email);
    }
}
