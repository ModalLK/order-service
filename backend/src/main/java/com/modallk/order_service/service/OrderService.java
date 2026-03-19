package com.modallk.order_service.service;

import com.modallk.order_service.dto.*;
import com.modallk.order_service.entity.*;
import com.modallk.order_service.exception.ResourceNotFoundException;
import com.modallk.order_service.repository.CartRepository;
import com.modallk.order_service.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final CartRepository cartRepository;
    private final ProductClientService productClientService;
    private final PaymentClientService paymentClientService;
    private final HttpServletRequest httpServletRequest;

    public OrderResponse placeOrder(PlaceOrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String authHeader = httpServletRequest.getHeader("Authorization");

        Cart cart = cartRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + email));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Please add items before placing an order.");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            var stockResult = productClientService.checkStock(
                    cartItem.getProductId(),
                    cartItem.getQuantity(),
                    authHeader
            );

            Boolean available = (Boolean) stockResult.get("available");
            if (available == null || !available) {
                throw new IllegalArgumentException("Insufficient stock for product: " + cartItem.getProductName());
            }
        }

        PaymentCheckoutRequestDto paymentRequest = new PaymentCheckoutRequestDto();

        PaymentCheckoutRequestDto.Customer customer = new PaymentCheckoutRequestDto.Customer();
        customer.setFullName(request.getCustomerName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getShippingAddress());
        customer.setCity(request.getCity());

        PaymentCheckoutRequestDto.PaymentInfo paymentInfo = new PaymentCheckoutRequestDto.PaymentInfo();
        paymentInfo.setMethod(request.getPaymentMethod());

        List<PaymentCheckoutRequestDto.CartItemDto> paymentItems = cart.getCartItems().stream()
                .map(item -> {
                    PaymentCheckoutRequestDto.CartItemDto dto = new PaymentCheckoutRequestDto.CartItemDto();
                    dto.setProductId(item.getProductId());
                    dto.setQty(item.getQuantity());
                    return dto;
                })
                .collect(Collectors.toList());

        paymentRequest.setCustomer(customer);
        paymentRequest.setPayment(paymentInfo);
        paymentRequest.setCart(paymentItems);

        PaymentResponseDto paymentResponse = paymentClientService.checkout(paymentRequest, authHeader);

        if (paymentResponse == null || !"PAID".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new IllegalArgumentException(
                    paymentResponse != null && paymentResponse.getFailureReason() != null
                            ? paymentResponse.getFailureReason()
                            : "Payment failed"
            );
        }

        for (CartItem cartItem : cart.getCartItems()) {
            productClientService.reduceStock(
                    cartItem.getProductId(),
                    cartItem.getQuantity(),
                    authHeader
            );
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
                .status(OrderStatus.CONFIRMED)
                .shippingAddress(request.getShippingAddress() + ", " + request.getCity())
                .totalAmount(totalAmount)
                .build();

        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return mapToOrderResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return orderRepository.findByUserEmail(email)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserEmail().equals(email)) {
            throw new IllegalArgumentException("You are not authorized to view this order.");
        }

        return mapToOrderResponse(order);
    }

    public OrderResponse cancelOrder(Long orderId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUserEmail().equals(email)) {
            throw new IllegalArgumentException("You are not authorized to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only PENDING or CONFIRMED orders can be cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getAdminOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapToOrderResponse(order);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

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