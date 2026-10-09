package com.example.orderservice.application.dto;

import java.util.List;

/**
 * GetProductsResponse
 */
public class GetPageProductsResponse {
    private final int page;
    private final int pageSize;
    private final int totalItem;
    private final int totalPage;
    private final List<GetProductItemResponse> productItems;

    public GetPageProductsResponse(int page, int pageSize, int totalItem, int totalPage,
            List<GetProductItemResponse> productItems) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalItem = totalItem;
        this.totalPage = totalPage;
        this.productItems = productItems;
    }

    public List<GetProductItemResponse> getProductItems() {
        return productItems;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItem() {
        return totalItem;
    }

    public int getTotalPage() {
        return totalPage;
    }

}
