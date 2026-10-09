package com.example.orderservice.application.service;

import java.util.List;

import com.example.orderservice.application.dto.GetPageProductsCommand;
import com.example.orderservice.application.dto.GetPageProductsResponse;
import com.example.orderservice.application.dto.GetProductItemResponse;
import com.example.orderservice.application.port.in.GetPageProductsUseCase;
import com.example.orderservice.application.port.out.ProductRepository;
import com.example.orderservice.domain.model.Product;

public class GetPageProductsService implements GetPageProductsUseCase {
    private final ProductRepository productRepository;

    public GetPageProductsService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public GetPageProductsResponse execute(GetPageProductsCommand command) {
        List<Product> productList = productRepository.findPage(command.getPage(), command.getPageSize());
        int totalItem = productRepository.countById();
        int totalPage = (totalItem + command.getPageSize() - 1) / command.getPageSize();
        List<GetProductItemResponse> productItems = productList.stream()
                .map(product -> new GetProductItemResponse(
                        product.getProductId(),
                        product.getProductName(),
                        product.getPrice().amount(),
                        product.getStock()))
                .toList();

        return new GetPageProductsResponse(
                command.getPage(),
                command.getPageSize(),
                totalItem,
                totalPage,
                productItems);
    }
}
