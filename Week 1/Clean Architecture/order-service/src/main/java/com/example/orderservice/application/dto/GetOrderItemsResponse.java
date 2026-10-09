package com.example.orderservice.application.dto;

/**
 * GetOrderItemsResponse
 */
public class GetOrderItemsResponse {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double price;

    public GetOrderItemsResponse(String productId, int quantity, double price, String productName) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getProductName() {
        return productName;
    }

}
