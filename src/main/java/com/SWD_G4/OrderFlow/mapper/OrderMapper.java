package com.SWD_G4.OrderFlow.mapper;

import com.SWD_G4.OrderFlow.dto.response.OrderItemResponse;
import com.SWD_G4.OrderFlow.dto.response.OrderResponse;
import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }
        
        List<OrderItemResponse> orderItemResponses = null;
        if (order.getOrderItems() != null) {
            orderItemResponses = order.getOrderItems().stream()
                    .map(this::toOrderItemResponse)
                    .collect(Collectors.toList());
        }
        
        String userName = null;
        if (order.getUser() != null) {
            userName = order.getUser().getFirstName() + " " + order.getUser().getLastName();
            if (userName.trim().isEmpty()) {
                userName = order.getUser().getUsername();
            }
        }
        
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .userName(userName)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .orderItems(orderItemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProduct() != null ? orderItem.getProduct().getId() : null)
                .productName(orderItem.getProduct() != null ? orderItem.getProduct().getName() : null)
                .productImageUrl(orderItem.getProduct() != null ? orderItem.getProduct().getImageUrl() : null)
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .totalPrice(orderItem.getTotalPrice())
                .createdAt(orderItem.getCreatedAt())
                .updatedAt(orderItem.getUpdatedAt())
                .build();
    }
}
