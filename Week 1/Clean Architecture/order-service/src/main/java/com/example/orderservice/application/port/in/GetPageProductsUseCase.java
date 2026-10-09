package com.example.orderservice.application.port.in;

import com.example.orderservice.application.dto.GetPageProductsCommand;
import com.example.orderservice.application.dto.GetPageProductsResponse;

public interface GetPageProductsUseCase {
    public GetPageProductsResponse execute(GetPageProductsCommand command);
}
