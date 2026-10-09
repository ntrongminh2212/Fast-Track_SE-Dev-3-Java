package com.example.orderservice.application.dto;

/**
 * GetProductItemResponse
 */
public class GetProductItemResponse {
    private final String productId;
    private final String productName;
    private final double price;
    private final int stock;

    public GetProductItemResponse(String productId, String productName, double price, int stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

}
