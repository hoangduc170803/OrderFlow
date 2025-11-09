package com.SWD_G4.OrderFlow.dto.request;

import com.SWD_G4.OrderFlow.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    
    private String notificationType; // FLORIST, CUSTOMER, STATUS_UPDATE
    private String orderNumber;
    private Long orderId;
    private String customerEmail;
    private String customerUsername;
    private String floristEmail;
    private String subject;
    private String message;
    private String orderStatus; // Order status as string
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private List<OrderItemInfo> orderItems;
    private String notes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemInfo implements Serializable {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}

