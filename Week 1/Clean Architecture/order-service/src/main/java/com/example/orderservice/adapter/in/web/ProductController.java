package com.example.orderservice.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.application.dto.GetPageProductsCommand;
import com.example.orderservice.application.dto.GetPageProductsResponse;
import com.example.orderservice.application.port.in.GetPageProductsUseCase;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final GetPageProductsUseCase getPageProductsUseCase;

    public ProductController(GetPageProductsUseCase getPageProductsUseCase) {
        this.getPageProductsUseCase = getPageProductsUseCase;
    }

    @GetMapping
    public ResponseEntity<GetPageProductsResponse> getPageProducts(
            @RequestParam(name = "page") @Min(1) int page,
            @RequestParam(name = "pageSize") @Min(10) int pageSize) {
        GetPageProductsCommand command = new GetPageProductsCommand(page, pageSize);
        return ResponseEntity.ok(getPageProductsUseCase.execute(command));
    }
}
