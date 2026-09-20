package com.example.solid.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.solid.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}
