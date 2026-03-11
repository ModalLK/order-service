package com.modallk.order_service.service;

import com.modallk.order_service.dto.OrderItemResponse;
import com.modallk.order_service.dto.OrderResponse;
import com.modallk.order_service.dto.PlaceOrderRequest;
import com.modallk.order_service.entity.Cart;
import com.modallk.order_service.entity.Order;
import com.modallk.order_service.entity.OrderItem;
import com.modallk.order_service.entity.OrderStatus;
import com.modallk.order_service.exception.ResourceNotFoundException;
import com.modallk.order_service.repository.CartRepository;
import com.modallk.order_service.repository.OrderItemRepository;
import com.modallk.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    // Place order from cart
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Please add items before placing an order.");
        }

        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .size(cartItem.getSize())
                        .subtotal(cartItem.getPrice() * cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        Double totalAmount = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();

        Order order = Order.builder()
                .userEmail(email)
                .orderItems(orderItems)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToOrderResponse(savedOrder);
    }

    // Get all orders for logged-in user
    public List<OrderResponse> getMyOrders() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return orderRepository.findByUserEmail(email)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // Get single order by ID
    public OrderResponse getOrderById(Long orderId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserEmail().equals(email)) {
            throw new IllegalArgumentException("You are not authorized to view this order.");
        }

        return mapToOrderResponse(order);
    }

    // Cancel order
    public OrderResponse cancelOrder(Long orderId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserEmail().equals(email)) {
            throw new IllegalArgumentException("You are not authorized to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    // Admin: Get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    // Admin: Get order by ID (no ownership check)
    public OrderResponse getAdminOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    // Admin: Update order status
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    // Helper: Map Order to OrderResponse
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .size(item.getSize())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUserEmail())
                .orderItems(itemResponses)
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
