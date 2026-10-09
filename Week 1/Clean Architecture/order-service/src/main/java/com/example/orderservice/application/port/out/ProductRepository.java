package com.example.orderservice.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.example.orderservice.domain.model.Product;

public interface ProductRepository {
    List<Product> findPage(int page, int pageSize);

    int countById();

    Map<String, String> findNamesByIds(Collection<String> idList);
}
