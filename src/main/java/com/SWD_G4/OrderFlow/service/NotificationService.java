package com.SWD_G4.OrderFlow.service;

import com.SWD_G4.OrderFlow.entity.Order;

/**
 * Service interface for sending notifications
 */
public interface NotificationService {
    
    /**
     * Send new order notification to florist
     * @param order the confirmed order
     */
    void sendFloristNotification(Order order);
    
    /**
     * Send order confirmation notification to customer
     * @param order the confirmed order
     */
    void sendCustomerNotification(Order order);
    
    /**
     * Send order status update notification to customer
     * @param order the order with updated status
     */
    void sendOrderStatusUpdateNotification(Order order);
}
