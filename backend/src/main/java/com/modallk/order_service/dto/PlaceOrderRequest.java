package com.modallk.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    private String phone;

    @NotBlank
    private String shippingAddress;

    @NotBlank
    private String city;

    @NotBlank
    private String paymentMethod;
}