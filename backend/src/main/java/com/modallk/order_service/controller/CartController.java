package com.modallk.order_service.controller;

import com.modallk.order_service.dto.*;
import com.modallk.order_service.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Cart management endpoints")
@SecurityRequirement(name = "Bearer Auth")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Adds a product to the logged-in user's cart.")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }
}
