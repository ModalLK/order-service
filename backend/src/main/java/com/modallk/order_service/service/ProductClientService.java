package com.modallk.order_service.service;

import com.modallk.order_service.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductClientService {

    private final RestTemplate restTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    public ProductResponse getProductById(Long productId, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                productServiceUrl + "/products/" + productId,
                HttpMethod.GET,
                entity,
                ProductResponse.class
        );

        return response.getBody();
    }

    public Map<String, Object> checkStock(Long productId, Integer quantity, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("productId", productId, "quantity", quantity),
                headers
        );

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                productServiceUrl + "/products/check-stock",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    public Map<String, Object> reduceStock(Long productId, Integer quantity, String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("productId", productId, "quantity", quantity),
                headers
        );

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                productServiceUrl + "/products/reduce-stock",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }
}