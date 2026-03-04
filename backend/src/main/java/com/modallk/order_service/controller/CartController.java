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
import com.modallk.order_service.dto.UpdateCartItemRequest;


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

    @GetMapping
    @Operation(summary = "Get my cart", description = "Returns the logged-in user's cart.")
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of a cart item.")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item", description = "Remove a specific item from the cart.")
    public ResponseEntity<CartResponse> removeCartItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(itemId));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the cart.")
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared successfully");
    }

    @GetMapping("/items/{itemId}")
    @Operation(summary = "Get cart item by ID", description = "Returns a single cart item by its ID.")
    public ResponseEntity<CartItemResponse> getCartItemById(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.getCartItemById(itemId));
    }

}
