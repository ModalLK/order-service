package com.modallk.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
}

