package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByUser(User user);
    
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems oi JOIN FETCH oi.product WHERE o.id = :id")
    Optional<Order> findByIdWithOrderItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findAllWithUser();
}
