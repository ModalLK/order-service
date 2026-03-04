package com.modallk.order_service.dto;

import com.modallk.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String userEmail;
    private List<OrderItemResponse> orderItems;
    private OrderStatus status;
    private String shippingAddress;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
