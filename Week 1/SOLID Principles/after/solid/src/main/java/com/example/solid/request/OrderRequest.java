package com.example.solid.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderRequest {

    @NotNull
    @Min(value = 0)
    private long id;
    @NotNull
    @Min(value = 0)
    private double total;
}
