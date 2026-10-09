package com.example.orderservice.adapter.out.persistent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.orderservice.adapter.out.persistent.entity.ProductEntity;
import com.example.orderservice.application.port.out.ProductRepository;
import com.example.orderservice.domain.model.Money;
import com.example.orderservice.domain.model.Product;

@Repository
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository productRepository;

    public JpaProductRepositoryAdapter(JpaProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findPage(int page, int pageSize) {
        Pageable pageable = PageRequest.of((page - 1), pageSize);
        return productRepository.findAll(pageable)
                .map(productEntity -> new Product(
                        productEntity.getId(),
                        productEntity.getProductName(),
                        new Money(productEntity.getPrice()),
                        productEntity.getStock()))
                .toList();
    }

    @Override
    public int countById() {
        return (int) productRepository.count();
    }

    @Override
    public Map<String, String> findNamesByIds(Collection<String> idList) {
        List<ProductEntity> productList = productRepository.findByIdIn(idList);
        return productList.stream().collect(Collectors.toMap(
                product -> product.getId(),
                product -> product.getProductName()));

    }

}
