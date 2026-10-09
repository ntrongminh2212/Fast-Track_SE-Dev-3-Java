package com.example.orderservice.domain.model;

public class Product {
    private String productId;
    private String productName;
    private Money price;
    private int stock;

    public Product(String productId, String productName, Money price, int stock) {
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

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

}
