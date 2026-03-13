package com.modallk.order_service.service;

import com.modallk.order_service.dto.*;
import com.modallk.order_service.entity.*;
import com.modallk.order_service.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClientService productClientService;
    private final HttpServletRequest httpServletRequest;

    public CartResponse addToCart(CartItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String authHeader = httpServletRequest.getHeader("Authorization");

        ProductResponse product = productClientService.getProductById(request.getProductId(), authHeader);

        if (product == null || product.getId() == null) {
            throw new RuntimeException("Product not found");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseGet(() -> Cart.builder()
                        .userEmail(email)
                        .build());

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId())
                        && item.getSize().equals(request.getSize()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQty) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            existingItem.setQuantity(newQty);
            existingItem.setPrice(product.getPrice());
            existingItem.setProductName(product.getName());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(request.getQuantity())
                    .size(request.getSize())
                    .build();

            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
        return mapToCartResponse(cart);
    }

    public CartResponse getMyCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        return mapToCartResponse(cart);
    }

    public CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        item.setQuantity(request.getQuantity());
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    public CartResponse removeCartItem(Long itemId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        cart.getCartItems().remove(item);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    public void clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public CartItemResponse getCartItemById(Long itemId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getCartItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + itemId));

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .size(item.getSize())
                .subtotal(item.getPrice() * item.getQuantity())
                .build();
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