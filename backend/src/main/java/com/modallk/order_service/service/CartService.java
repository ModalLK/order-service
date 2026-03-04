package com.modallk.order_service.service;

import com.modallk.order_service.dto.*;
import com.modallk.order_service.entity.*;
import com.modallk.order_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartResponse addToCart(CartItemRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Get existing cart or create new one
        Cart cart = cartRepository.findByUserEmail(email)
                .orElseGet(() -> Cart.builder()
                        .userEmail(email)
                        .build());

        // Check if same product+size already in cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId())
                        && item.getSize().equals(request.getSize()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Just increase quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .price(request.getPrice())
                    .quantity(request.getQuantity())
                    .size(request.getSize())
                    .build();
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .size(item.getSize())
                        .subtotal(item.getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        Double total = items.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userEmail(cart.getUserEmail())
                .cartItems(items)
                .totalAmount(total)
                .build();
    }
}
