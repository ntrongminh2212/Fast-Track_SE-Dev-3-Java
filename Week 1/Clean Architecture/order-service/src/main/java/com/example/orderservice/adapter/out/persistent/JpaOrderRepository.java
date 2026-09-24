package com.example.orderservice.adapter.out.persistent;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {

}
