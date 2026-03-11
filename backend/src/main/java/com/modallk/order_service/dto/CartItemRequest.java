package com.modallk.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    private Double price;

    @Min(1)
    @NotNull
    private Integer quantity;

    @NotBlank
    private String size; // S, M, L, XL
}
