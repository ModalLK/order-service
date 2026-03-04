package com.modallk.order_service.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long id;
    private String userEmail;
    private List<CartItemResponse> cartItems;
    private Double totalAmount;
}

