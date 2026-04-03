package com.modallk.order_service.service;

import com.modallk.order_service.dto.PaymentCheckoutRequestDto;
import com.modallk.order_service.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PaymentClientService {

    private final RestTemplate restTemplate;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public PaymentResponseDto checkout(PaymentCheckoutRequestDto request, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<PaymentCheckoutRequestDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PaymentResponseDto> response = restTemplate.exchange(
                paymentServiceUrl + "/api/payments/checkout",
                HttpMethod.POST,
                entity,
                PaymentResponseDto.class
        );

        return response.getBody();
    }
}