package com.SWD_G4.OrderFlow.service;

import com.SWD_G4.OrderFlow.dto.request.CreateOrderRequest;
import com.SWD_G4.OrderFlow.dto.response.OrderResponse;
import com.SWD_G4.OrderFlow.entity.*;
import com.SWD_G4.OrderFlow.exception.AppException;
import com.SWD_G4.OrderFlow.exception.ErrorCode;
import com.SWD_G4.OrderFlow.mapper.OrderMapper;
import com.SWD_G4.OrderFlow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    
    public OrderResponse createOrder(User user, CreateOrderRequest request) {
        // Get user's cart with items
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new AppException(ErrorCode.EMPTY_CART);
        }
        
        // Validate stock for all items in cart
        validateStockAvailability(cart.getCartItems());
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .totalAmount(cart.getTotalAmount())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .notes(request.getNotes())
                .paymentMethod(request.getPaymentMethod())
                .build();
        
        order = orderRepository.save(order);
        
        // Create order items from cart items
        final Order finalOrder = order;
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = OrderItem.builder()
                            .order(finalOrder)
                            .product(cartItem.getProduct())
                            .quantity(cartItem.getQuantity())
                            .unitPrice(cartItem.getUnitPrice())
                            .totalPrice(cartItem.getTotalPrice())
                            .build();
                    
                    return orderItemRepository.save(orderItem);
                })
                .toList();
        
        order.setOrderItems(orderItems);
        
        // Process COD flow
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            processCODOrder(order);
        }
        
        // Clear cart after successful order creation
        cartItemRepository.deleteByCart(cart);
        cart.calculateTotalAmount();
        cartRepository.save(cart);
        
        log.info("Order created successfully: {}", orderNumber);
        
        return orderMapper.toOrderResponse(order);
    }
    
    public OrderResponse confirmCODOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Verify payment method is COD
        if (order.getPaymentMethod() != PaymentMethod.COD) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        
        // Verify order is in PENDING status
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CONFIRMED);
        }
        
        // Update order status to CONFIRMED
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order = orderRepository.save(order);
        
        // Decrement product inventory
        decrementProductInventory(order.getOrderItems());
        
        // TODO: Send notification to florist
        sendFloristNotification(order);
        
        // TODO: Send confirmation notification to customer
        sendCustomerNotification(order);
        
        log.info("COD order confirmed: {}", order.getOrderNumber());
        
        return orderMapper.toOrderResponse(order);
    }
    
    public OrderResponse getOrder(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return orderMapper.toOrderResponse(order);
    }
    
    public List<OrderResponse> getUserOrders(User user) {
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }
    
    private void validateStockAvailability(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
    }
    
    private void processCODOrder(Order order) {
        // For COD orders, we don't need to process payment
        // The order will be confirmed when customer confirms on the site
        log.info("COD order created, waiting for customer confirmation: {}", order.getOrderNumber());
    }
    
    private void decrementProductInventory(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            Product product = orderItem.getProduct();
            int newStock = product.getStockQuantity() - orderItem.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);
            
            log.info("Decremented stock for product {}: {} -> {}", 
                    product.getName(), orderItem.getQuantity(), newStock);
        }
    }
    
    private void sendFloristNotification(Order order) {
        // TODO: Implement notification service
        log.info("Sending new order notification to florist for order: {}", order.getOrderNumber());
    }
    
    private void sendCustomerNotification(Order order) {
        // TODO: Implement notification service
        log.info("Sending order confirmation to customer: {}", order.getUser().getUsername());
    }
    
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }
}
