package com.example.orderservice.adapter.out.persistent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.orderservice.adapter.out.persistent.entity.OrderEntity;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, String> {

    @Query(value = "SELECT nextval('order_id_seq')", nativeQuery = true)
    Long nextOrderId();
}
