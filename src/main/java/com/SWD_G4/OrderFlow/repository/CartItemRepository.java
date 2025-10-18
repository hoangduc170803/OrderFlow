package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.Cart;
import com.SWD_G4.OrderFlow.entity.CartItem;
import com.SWD_G4.OrderFlow.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    void deleteByCartAndProduct(Cart cart, Product product);
    
    void deleteByCart(Cart cart);
}
