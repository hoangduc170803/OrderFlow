package com.SWD_G4.OrderFlow.service;

import com.SWD_G4.OrderFlow.dto.request.AddToCartRequest;
import com.SWD_G4.OrderFlow.dto.request.UpdateCartItemRequest;
import com.SWD_G4.OrderFlow.dto.response.CartResponse;
import com.SWD_G4.OrderFlow.entity.*;
import com.SWD_G4.OrderFlow.exception.AppException;
import com.SWD_G4.OrderFlow.exception.ErrorCode;
import com.SWD_G4.OrderFlow.mapper.CartMapper;
import com.SWD_G4.OrderFlow.repository.CartItemRepository;
import com.SWD_G4.OrderFlow.repository.CartRepository;
import com.SWD_G4.OrderFlow.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;
    private final EntityManager entityManager;
    
    public CartResponse getCart(User user) {
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseGet(() -> createCartForUser(user));
        
        cart.calculateTotalAmount();
        cartRepository.save(cart);
        
        return cartMapper.toCartResponse(cart);
    }
    
    public CartResponse addToCart(User user, AddToCartRequest request) {
        log.info("Adding product {} with quantity {} to cart for user {}", 
                request.getProductId(), request.getQuantity(), user.getUsername());
        
        // Validate product exists and is active (with cache support)
        Product product = productService.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        
        if (!product.getIsActive()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_AVAILABLE);
        }
        
        // Check stock availability
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        // Get or create cart for user
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseGet(() -> createCartForUser(user));
        
        log.info("Cart ID: {}, Current cart items count: {}", 
                cart.getId(), cart.getCartItems() != null ? cart.getCartItems().size() : 0);
        
        // Check if product already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingCartItem.isPresent()) {
            // Update existing cart item
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Check total stock availability
            if (product.getStockQuantity() < newQuantity) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
            
            cartItem.setQuantity(newQuantity);
            cartItem.calculateTotalPrice();
            cartItemRepository.save(cartItem);
            log.info("Updated existing cart item, new quantity: {}", newQuantity);
        } else {
            // Create new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            
            // Calculate total price manually
            cartItem.calculateTotalPrice();
            cartItemRepository.save(cartItem);
            log.info("Created new cart item with ID: {}", cartItem.getId());
        }
        
        // Flush any pending changes to database
        entityManager.flush();
        
        // Clear the persistence context to force fresh data from database
        entityManager.clear();
        
        // Refresh cart to get updated cart items and recalculate total
        cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        // Recalculate cart total
        cart.calculateTotalAmount();
        cartRepository.save(cart);
        
        log.info("Cart updated - Items count: {}, Total amount: {}", 
                cart.getCartItems() != null ? cart.getCartItems().size() : 0, cart.getTotalAmount());
        
        return cartMapper.toCartResponse(cart);
    }
    
    public CartResponse updateCartItem(User user, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        
        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        
        // Check stock availability
        if (cartItem.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItem.calculateTotalPrice();
        cartItemRepository.save(cartItem);
        
        // Recalculate cart total
        cart.calculateTotalAmount();
        cartRepository.save(cart);
        
        return cartMapper.toCartResponse(cart);
    }
    
    public CartResponse removeFromCart(User user, Long cartItemId) {
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        
        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        
        cartItemRepository.delete(cartItem);
        
        // Recalculate cart total
        cart.calculateTotalAmount();
        cartRepository.save(cart);
        
        return cartMapper.toCartResponse(cart);
    }
    
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        cartItemRepository.deleteByCart(cart);
        
        cart.calculateTotalAmount();
        cartRepository.save(cart);
    }
    
    private Cart createCartForUser(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();
        
        return cartRepository.save(cart);
    }
}
