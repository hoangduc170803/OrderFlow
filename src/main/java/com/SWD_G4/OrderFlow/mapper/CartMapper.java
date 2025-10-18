package com.SWD_G4.OrderFlow.mapper;

import com.SWD_G4.OrderFlow.dto.response.CartItemResponse;
import com.SWD_G4.OrderFlow.dto.response.CartResponse;
import com.SWD_G4.OrderFlow.entity.Cart;
import com.SWD_G4.OrderFlow.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    
    public CartResponse toCartResponse(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        List<CartItemResponse> cartItemResponses = null;
        if (cart.getCartItems() != null) {
            cartItemResponses = cart.getCartItems().stream()
                    .map(this::toCartItemResponse)
                    .collect(Collectors.toList());
        }
        
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser() != null ? cart.getUser().getId() : null)
                .cartItems(cartItemResponses)
                .totalAmount(cart.getTotalAmount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
    
    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null)
                .productName(cartItem.getProduct() != null ? cartItem.getProduct().getName() : null)
                .productImageUrl(cartItem.getProduct() != null ? cartItem.getProduct().getImageUrl() : null)
                .unitPrice(cartItem.getUnitPrice())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }
}
