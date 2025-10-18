package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.request.AddToCartRequest;
import com.SWD_G4.OrderFlow.dto.request.UpdateCartItemRequest;
import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.dto.response.CartResponse;
import com.SWD_G4.OrderFlow.entity.User;
import com.SWD_G4.OrderFlow.repository.UserRepository;
import com.SWD_G4.OrderFlow.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;
    private final UserRepository userRepository;
    
    private User getCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        CartResponse cart = cartService.getCart(user);
        
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Get cart successfully")
                .result(cart)
                .build());
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        CartResponse cart = cartService.addToCart(user, request);
        
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Product added to cart successfully")
                .result(cart)
                .build());
    }
    
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        CartResponse cart = cartService.updateCartItem(user, cartItemId, request);
        
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Cart item updated successfully")
                .result(cart)
                .build());
    }
    
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long cartItemId,
            Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        CartResponse cart = cartService.removeFromCart(user, cartItemId);
        
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(1000)
                .message("Product removed from cart successfully")
                .result(cart)
                .build());
    }
    
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        cartService.clearCart(user);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(1000)
                .message("Cart cleared successfully")
                .build());
    }
}
