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
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;
    private final EntityManager entityManager;
    
    @Transactional(readOnly = true)
    public CartResponse getCart(User user) {
        // Always return a fresh, recalculated cart snapshot
        // Create cart if missing, then refresh from DB
        cartRepository.findByUser(user).orElseGet(() -> createCartForUser(user));
        return refreshCartResponse(user);
    }
    
    @Transactional
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
        
        CartResponse response = refreshCartResponse(user);
        
        log.info("Cart updated - Total amount: {}", response.getTotalAmount());
        
        return response;
    }
    
    @Transactional
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
        
        return refreshCartResponse(user);
    }
    
    @Transactional
    public CartResponse removeFromCart(User user, Long cartItemId) {
        log.info("Attempting to delete cart item ID: {} for user: {}", cartItemId, user.getUsername());
        
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        log.info("Found cart ID: {} with {} items", cart.getId(), 
                cart.getCartItems() != null ? cart.getCartItems().size() : 0);
        
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
        
        // Verify cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            log.error("Cart item {} does not belong to cart {}", cartItemId, cart.getId());
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        
        log.info("Deleting cart item - Product: {}, Quantity: {}", 
                cartItem.getProduct().getName(), cartItem.getQuantity());
        
        // Remove from cart's collection first (triggers orphanRemoval)
        if (cart.getCartItems() != null) {
            boolean removed = cart.getCartItems().remove(cartItem);
            log.info("Removed from collection: {}", removed);
        }
        
        // Save cart to persist the collection change
        cartRepository.save(cart);
        
        // Then delete explicitly from repository
        cartItemRepository.delete(cartItem);
        
        // Flush to database immediately
        entityManager.flush();
        
        log.info("Cart item deleted and flushed to database");
        
        return refreshCartResponse(user);
    }
    
    @Transactional
    public void clearCart(User user) {
        log.info("Attempting to clear cart for user: {}", user.getUsername());
        
        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        int itemCount = cart.getCartItems() != null ? cart.getCartItems().size() : 0;
        log.info("Found cart ID: {} with {} items to delete", cart.getId(), itemCount);
        
        // Clear the collection first (triggers orphanRemoval due to cascade)
        if (cart.getCartItems() != null && !cart.getCartItems().isEmpty()) {
            cart.getCartItems().clear();
            log.info("Cleared cart items collection");
        }
        
        // Save cart to persist the collection change
        cartRepository.save(cart);
        
        // Then delete all items explicitly from repository
        cartItemRepository.deleteByCart(cart);
        
        // Flush to database immediately
        entityManager.flush();
        
        log.info("Cart cleared successfully - {} items deleted", itemCount);
        
        refreshCartResponse(user);
    }
    
    private Cart createCartForUser(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();
        
        return cartRepository.save(cart);
    }
    
    private CartResponse refreshCartResponse(User user) {
        entityManager.flush();
        entityManager.clear();
        
        Cart refreshedCart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        
        refreshedCart.calculateTotalAmount();
        cartRepository.save(refreshedCart);
        
        return cartMapper.toCartResponse(refreshedCart);
    }
}
