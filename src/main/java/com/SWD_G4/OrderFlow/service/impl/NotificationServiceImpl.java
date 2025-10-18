package com.SWD_G4.OrderFlow.service.impl;

import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.User;
import com.SWD_G4.OrderFlow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.notification.email.from:noreply@orderflow.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.florist:florist@orderflow.com}")
    private String floristEmail;
    
    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;
    
    @Override
    public void sendFloristNotification(Order order) {
        if (!notificationEnabled) {
            log.info("Notifications disabled, skipping florist notification for order: {}", order.getOrderNumber());
            return;
        }
        
        try {
            String subject = "New Order Confirmed - " + order.getOrderNumber();
            String content = buildFloristEmailContent(order);
            
            sendHtmlEmail(floristEmail, subject, content);
            log.info("Florist email notification sent successfully for order: {}", order.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to send florist notification for order: {}", order.getOrderNumber(), e);
        }
    }
    
    @Override
    public void sendCustomerNotification(Order order) {
        if (!notificationEnabled) {
            log.info("Notifications disabled, skipping customer notification for order: {}", order.getOrderNumber());
            return;
        }
        
        User customer = order.getUser();
        if (customer == null || !StringUtils.hasText(customer.getEmail())) {
            log.warn("Customer email not available for order: {}", order.getOrderNumber());
            return;
        }
        
        try {
            String subject = "Order Confirmed - " + order.getOrderNumber();
            String content = buildCustomerEmailContent(order);
            
            sendHtmlEmail(customer.getEmail(), subject, content);
            log.info("Customer email notification sent successfully for order: {} to email: {}", 
                    order.getOrderNumber(), customer.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send customer notification for order: {} to email: {}", 
                    order.getOrderNumber(), customer.getEmail(), e);
        }
    }
    
    @Override
    public void sendOrderStatusUpdateNotification(Order order) {
        if (!notificationEnabled) {
            log.info("Notifications disabled, skipping status update notification for order: {}", order.getOrderNumber());
            return;
        }
        
        User customer = order.getUser();
        if (customer == null || !StringUtils.hasText(customer.getEmail())) {
            log.warn("Customer email not available for order status update: {}", order.getOrderNumber());
            return;
        }
        
        try {
            String subject = "Order Status Update - " + order.getOrderNumber();
            String content = buildOrderStatusUpdateEmailContent(order);
            
            sendHtmlEmail(customer.getEmail(), subject, content);
            log.info("Order status update notification sent successfully for order: {} to email: {}", 
                    order.getOrderNumber(), customer.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send order status update notification for order: {} to email: {}", 
                    order.getOrderNumber(), customer.getEmail(), e);
        }
    }
    
    private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true indicates HTML content
        
        mailSender.send(message);
    }
    
    private String buildFloristEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>New Order Confirmed</h2>");
        content.append("<p>A new order has been confirmed and requires your attention.</p>");
        
        // Order Details
        content.append("<h3>Order Details</h3>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        content.append("<tr><td><strong>Order Number:</strong></td><td>").append(order.getOrderNumber()).append("</td></tr>");
        content.append("<tr><td><strong>Customer:</strong></td><td>").append(order.getUser().getUsername()).append("</td></tr>");
        content.append("<tr><td><strong>Total Amount:</strong></td><td>$").append(order.getTotalAmount()).append("</td></tr>");
        content.append("<tr><td><strong>Payment Method:</strong></td><td>").append(order.getPaymentMethod()).append("</td></tr>");
        content.append("<tr><td><strong>Shipping Address:</strong></td><td>").append(order.getShippingAddress()).append("</td></tr>");
        content.append("<tr><td><strong>Order Date:</strong></td><td>").append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        if (StringUtils.hasText(order.getNotes())) {
            content.append("<tr><td><strong>Notes:</strong></td><td>").append(order.getNotes()).append("</td></tr>");
        }
        content.append("</table>");
        
        // Order Items
        content.append("<h3>Order Items</h3>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        content.append("<tr><th>Product</th><th>Quantity</th><th>Unit Price</th><th>Total</th></tr>");
        
        if (order.getOrderItems() != null) {
            for (var item : order.getOrderItems()) {
                content.append("<tr>");
                content.append("<td>").append(item.getProduct().getName()).append("</td>");
                content.append("<td>").append(item.getQuantity()).append("</td>");
                content.append("<td>$").append(item.getUnitPrice()).append("</td>");
                content.append("<td>$").append(item.getTotalPrice()).append("</td>");
                content.append("</tr>");
            }
        }
        content.append("</table>");
        
        content.append("<p><strong>Action Required:</strong> Please prepare the order for delivery.</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    private String buildCustomerEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>Order Confirmed Successfully!</h2>");
        content.append("<p>Dear ").append(order.getUser().getUsername()).append(",</p>");
        content.append("<p>Thank you for your order! Your order has been confirmed and is being prepared.</p>");
        
        // Order Details
        content.append("<h3>Order Details</h3>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        content.append("<tr><td><strong>Order Number:</strong></td><td>").append(order.getOrderNumber()).append("</td></tr>");
        content.append("<tr><td><strong>Total Amount:</strong></td><td>$").append(order.getTotalAmount()).append("</td></tr>");
        content.append("<tr><td><strong>Payment Method:</strong></td><td>").append(order.getPaymentMethod()).append("</td></tr>");
        content.append("<tr><td><strong>Shipping Address:</strong></td><td>").append(order.getShippingAddress()).append("</td></tr>");
        content.append("<tr><td><strong>Order Date:</strong></td><td>").append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        content.append("<tr><td><strong>Status:</strong></td><td>").append(order.getStatus()).append("</td></tr>");
        if (StringUtils.hasText(order.getNotes())) {
            content.append("<tr><td><strong>Notes:</strong></td><td>").append(order.getNotes()).append("</td></tr>");
        }
        content.append("</table>");
        
        // Order Items
        content.append("<h3>Order Items</h3>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        content.append("<tr><th>Product</th><th>Quantity</th><th>Unit Price</th><th>Total</th></tr>");
        
        if (order.getOrderItems() != null) {
            for (var item : order.getOrderItems()) {
                content.append("<tr>");
                content.append("<td>").append(item.getProduct().getName()).append("</td>");
                content.append("<td>").append(item.getQuantity()).append("</td>");
                content.append("<td>$").append(item.getUnitPrice()).append("</td>");
                content.append("<td>$").append(item.getTotalPrice()).append("</td>");
                content.append("</tr>");
            }
        }
        content.append("</table>");
        
        if (order.getPaymentMethod().name().equals("COD")) {
            content.append("<p><strong>Payment:</strong> You will pay when the order is delivered.</p>");
        }
        
        content.append("<p>We will notify you when your order is ready for delivery.</p>");
        content.append("<p>Thank you for choosing our service!</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
    
    private String buildOrderStatusUpdateEmailContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("<html><body>");
        content.append("<h2>Order Status Update</h2>");
        content.append("<p>Dear ").append(order.getUser().getUsername()).append(",</p>");
        content.append("<p>Your order status has been updated.</p>");
        
        // Order Details
        content.append("<h3>Order Details</h3>");
        content.append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>");
        content.append("<tr><td><strong>Order Number:</strong></td><td>").append(order.getOrderNumber()).append("</td></tr>");
        content.append("<tr><td><strong>New Status:</strong></td><td>").append(order.getStatus()).append("</td></tr>");
        content.append("<tr><td><strong>Updated Date:</strong></td><td>").append(order.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        content.append("</table>");
        
        // Status-specific messages
        switch (order.getStatus()) {
            case SHIPPED:
                content.append("<p>Your order has been shipped and is on its way to you!</p>");
                break;
            case DELIVERED:
                content.append("<p>Your order has been delivered successfully. Thank you for your business!</p>");
                break;
            case CANCELLED:
                content.append("<p>Your order has been cancelled. If you have any questions, please contact our support team.</p>");
                break;
            default:
                content.append("<p>Your order status has been updated to: ").append(order.getStatus()).append("</p>");
        }
        
        content.append("<p>Thank you for choosing our service!</p>");
        content.append("</body></html>");
        
        return content.toString();
    }
}
