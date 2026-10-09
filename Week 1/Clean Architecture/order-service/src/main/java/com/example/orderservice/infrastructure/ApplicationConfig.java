package com.example.orderservice.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.orderservice.application.port.in.CreateOrderUseCase;
import com.example.orderservice.application.port.in.GetOrderUseCase;
import com.example.orderservice.application.port.in.GetPageProductsUseCase;
import com.example.orderservice.application.port.out.OrderRepository;
import com.example.orderservice.application.port.out.ProductRepository;
import com.example.orderservice.application.service.CreateOrderService;
import com.example.orderservice.application.service.GetOrderService;
import com.example.orderservice.application.service.GetPageProductsService;
import com.example.orderservice.application.service.OrderValidator;

@Configuration
public class ApplicationConfig {
    @Bean
    public OrderValidator orderValidator() {
        return new OrderValidator();
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderRepository orderRepository,
            OrderValidator validator) {
        return new CreateOrderService(orderRepository, validator);
    }

    @Bean
    public GetPageProductsUseCase getPageProductsUseCase(
            ProductRepository productRepository) {
        return new GetPageProductsService(productRepository);
    }

    @Bean
    public GetOrderUseCase getOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository) {
        return new GetOrderService(orderRepository, productRepository);
    }
}
