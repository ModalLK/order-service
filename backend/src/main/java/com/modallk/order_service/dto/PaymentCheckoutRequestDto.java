package com.modallk.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaymentCheckoutRequestDto {
    private Customer customer;
    private PaymentInfo payment;
    private List<CartItemDto> cart;

    @Data
    public static class Customer {
        private String fullName;
        private String phone;
        private String address;
        private String city;
    }

    @Data
    public static class PaymentInfo {
        private String method;
    }

    @Data
    public static class CartItemDto {
        private Long productId;
        private Integer qty;
    }
}