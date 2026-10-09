package com.example.orderservice.application.dto;

import java.util.List;

/**
 * GetOrderResponse
 */
public class GetOrderResponse {
    private final String orderId;
    private final double total;
    private final String status;
    private final List<GetOrderItemsResponse> items;

    public GetOrderResponse(String orderId, double total, String status, List<GetOrderItemsResponse> items) {
        this.orderId = orderId;
        this.total = total;
        this.status = status;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public List<GetOrderItemsResponse> getItems() {
        return items;
    }

}
