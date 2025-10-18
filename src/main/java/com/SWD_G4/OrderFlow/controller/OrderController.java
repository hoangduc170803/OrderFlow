package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.request.CreateOrderRequest;
import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.dto.response.OrderResponse;
import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.User;
import com.SWD_G4.OrderFlow.exception.AppException;
import com.SWD_G4.OrderFlow.exception.ErrorCode;
import com.SWD_G4.OrderFlow.repository.UserRepository;
import com.SWD_G4.OrderFlow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    private final UserRepository userRepository;
    
    private User extractUserFromAuthentication(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        User user = extractUserFromAuthentication(authentication);
        
        OrderResponse order = orderService.createOrder(user, request);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(1000)
                .message("Order created successfully")
                .result(order)
                .build());
    }
    
    @PostMapping("/{orderId}/confirm-cod")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmCODOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        User user = extractUserFromAuthentication(authentication);
        
        OrderResponse order = orderService.confirmCODOrder(user, orderId);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(1000)
                .message("COD order confirmed successfully")
                .result(order)
                .build());
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        User user = extractUserFromAuthentication(authentication);
        
        OrderResponse order = orderService.getOrder(user, orderId);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(1000)
                .message("Get order successfully")
                .result(order)
                .build());
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders(Authentication authentication) {
        User user = extractUserFromAuthentication(authentication);
        
        List<OrderResponse> orders = orderService.getUserOrders(user);
        
        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .code(1000)
                .message("Get user orders successfully")
                .result(orders)
                .build());
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam Order.OrderStatus status,
            Authentication authentication) {
        User user = extractUserFromAuthentication(authentication);
        
        // Check if user is a florist (can update any order status)
        boolean isFlorist = user.getRoles() != null && 
                user.getRoles().stream()
                        .anyMatch(role -> "FLORIST".equals(role.getName()));
        
        if (!isFlorist) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        OrderResponse order = orderService.updateOrderStatus(orderId, status);
        
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .code(1000)
                .message("Order status updated successfully")
                .result(order)
                .build());
    }
}
