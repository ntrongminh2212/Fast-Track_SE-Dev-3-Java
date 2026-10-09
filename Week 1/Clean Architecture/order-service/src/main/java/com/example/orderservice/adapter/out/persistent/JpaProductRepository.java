package com.example.orderservice.adapter.out.persistent;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.orderservice.adapter.out.persistent.entity.ProductEntity;

public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {

    List<ProductEntity> findByIdIn(Collection<String> idList);

}
