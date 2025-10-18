package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.Cart;
import com.SWD_G4.OrderFlow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(@Param("user") User user);
    
    boolean existsByUser(User user);
}
