package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    
    void deleteByOrder(Order order);
}
