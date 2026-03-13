package com.modallk.order_service.dto;

import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private String customerName;
    private String status;
    private String paymentMethod;
    private String gatewayTransactionId;
    private String failureReason;
}