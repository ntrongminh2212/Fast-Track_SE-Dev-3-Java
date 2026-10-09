package com.example.orderservice.application.dto;

/**
 * GetProductsCommand
 */
public class GetPageProductsCommand {
    private final int page;
    private final int pageSize;

    public GetPageProductsCommand(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

}
