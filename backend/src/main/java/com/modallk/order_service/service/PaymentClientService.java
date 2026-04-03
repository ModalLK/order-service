package com.modallk.order_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modallk.order_service.dto.PaymentCheckoutRequestDto;
import com.modallk.order_service.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public PaymentResponseDto checkout(PaymentCheckoutRequestDto request, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.ALL));
        headers.set("Authorization", authHeader);

        HttpEntity<PaymentCheckoutRequestDto> entity = new HttpEntity<>(request, headers);

        try {
            // Receive as String to bypass content-type negotiation
            ResponseEntity<String> response = restTemplate.exchange(
                    paymentServiceUrl + "/payments/checkout",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                throw new IllegalArgumentException("Payment service returned empty response");
            }

            return objectMapper.readValue(body, PaymentResponseDto.class);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new IllegalArgumentException(
                "Payment service error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString()
            );
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new IllegalArgumentException(
                "Payment service unreachable. Please try again: " + e.getMessage()
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to process payment response: " + e.getMessage()
            );
        }
    }
}