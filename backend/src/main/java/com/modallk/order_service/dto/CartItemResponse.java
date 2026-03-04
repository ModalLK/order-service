package com.modallk.order_service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private String size;
    private Double subtotal;
}
