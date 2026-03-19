package com.modallk.order_service.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private Double price;
    private Integer stock;
    private String category;
    private String description;
    private String imageUrl;
}