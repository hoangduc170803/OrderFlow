package com.SWD_G4.OrderFlow.service.impl;

import com.SWD_G4.OrderFlow.configuration.KafkaConfig;
import com.SWD_G4.OrderFlow.constant.PredefinedRole;
import com.SWD_G4.OrderFlow.dto.request.NotificationMessage;
import com.SWD_G4.OrderFlow.entity.Order;
import com.SWD_G4.OrderFlow.entity.User;
import com.SWD_G4.OrderFlow.repository.UserRepository;
import com.SWD_G4.OrderFlow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepository userRepository;
    
    @Value("${app.notification.email.from:noreply@orderflow.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.florist:}")
    private String fallbackFloristEmail; // Fallback email if no florists found
    
    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;
    
    @Value("${app.notification.kafka.enabled:true}")
    private boolean kafkaEnabled;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.email.fallback:true}")
    private boolean emailFallback;
    
    @Override
    public void sendFloristNotification(Order order) {
        if (!notificationEnabled) {
            log.info("Notifications disabled, skipping florist notification for order: {}", order.getOrderNumber());
            return;
        }
        
        // Get all users with FLORIST role
        List<User> florists = userRepository.findByRoleName(PredefinedRole.FLORIST_ROLE);
        
        if (florists.isEmpty()) {
            log.warn("No florists found in database for order: {}. Using fallback email if configured.", order.getOrderNumber());
            // Fallback to configured email if no florists found
            if (StringUtils.hasText(fallbackFloristEmail)) {
                sendNotificationToFlorist(order, fallbackFloristEmail);
            } else {
                log.error("No florists found and no fallback email configured for order: {}", order.getOrderNumber());
            }
            return;
        }
        
        log.info("Sending florist notification to {} florist(s) for order: {}", florists.size(), order.getOrderNumber());
        
        // Send notification to each florist
        for (User florist : florists) {
            if (StringUtils.hasText(florist.getEmail())) {
                sendNotificationToFlorist(order, florist.getEmail());
            } else {
                log.warn("Florist {} (ID: {}) has no email address, skipping notification",
                        florist.getUsername(), florist.getId());
            }
        }
    }
    
    /**
     * Send notification to a single florist (Kafka + Email)
     */
    private void sendNotificationToFlorist(Order order, String floristEmail) {
        // Send real-time notification via Kafka
        if (kafkaEnabled) {
            sendKafkaNotification(order, "FLORIST", floristEmail);
        }
        
        // Send email notification (always send if enabled, not just as fallback)
        if (emailEnabled) {
            try {
                String subject = "New Order Confirmed - " + order.getOrderNumber();
                String content = buildFloristEmailContent(order);
                
                sendHtmlEmail(floristEmail, subject, content);
                log.info("Florist email notification sent successfully for order: {} to email: {}", 
                        order.getOrderNumber(), floristEmail);
                
            } catch (Exception e) {
                log.error("Failed to send florist email notification for order: {} to email: {}", 
                        order.getOrderNumber(), floristEmail, e);
            }
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
        
//         Send real-time notification via Kafka
        if (kafkaEnabled) {
            sendKafkaNotification(order, "CUSTOMER", customer.getEmail());
        }
        
        // Send email notification (always send if enabled, not just as fallback)
        if (emailEnabled) {
            try {
                String subject = "Order Confirmed - " + order.getOrderNumber();
                String content = buildCustomerEmailContent(order);
                
                sendHtmlEmail(customer.getEmail(), subject, content);
                log.info("Customer email notification sent successfully for order: {} to email: {}", 
                        order.getOrderNumber(), customer.getEmail());
                
            } catch (Exception e) {
                log.error("Failed to send customer email notification for order: {} to email: {}", 
                        order.getOrderNumber(), customer.getEmail(), e);
            }
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
        
        // Send real-time notification via Kafka
        if (kafkaEnabled) {
            sendKafkaNotification(order, "STATUS_UPDATE", customer.getEmail());
        }
        
        // Send email notification (always send if enabled, not just as fallback)
        if (emailEnabled) {
            try {
                String subject = "Order Status Update - " + order.getOrderNumber();
                String content = buildOrderStatusUpdateEmailContent(order);
                
                sendHtmlEmail(customer.getEmail(), subject, content);
                log.info("Order status update notification sent successfully for order: {} to email: {}", 
                        order.getOrderNumber(), customer.getEmail());
                
            } catch (Exception e) {
                log.error("Failed to send order status update email notification for order: {} to email: {}", 
                        order.getOrderNumber(), customer.getEmail(), e);
            }
        }
    }
    
    /**
     * Send notification via Kafka for real-time processing
     */
    private void sendKafkaNotification(Order order, String notificationType, String recipientEmail) {
        try {
            NotificationMessage message = buildNotificationMessage(order, notificationType, recipientEmail);
            String topic = getTopicForNotificationType(notificationType);
            String key = order.getOrderNumber();
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Kafka notification sent successfully for order: {} to topic: {} with offset: {}", 
                            order.getOrderNumber(), topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send Kafka notification for order: {} to topic: {}", 
                            order.getOrderNumber(), topic, ex);
                    // Optional: Fallback to email if Kafka fails and email fallback is enabled
                    // Note: Email is already sent independently if emailEnabled is true
                    if (emailFallback && !emailEnabled) {
                        log.info("Falling back to email notification for order: {}", order.getOrderNumber());
                        sendEmailFallback(order, notificationType, recipientEmail);
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("Error sending Kafka notification for order: {}", order.getOrderNumber(), e);
            // Optional: Fallback to email if Kafka fails and email fallback is enabled
            // Note: Email is already sent independently if emailEnabled is true
            if (emailFallback && !emailEnabled) {
                sendEmailFallback(order, notificationType, recipientEmail);
            }
        }
    }
    
    private NotificationMessage buildNotificationMessage(Order order, String notificationType, String recipientEmail) {
        User customer = order.getUser();
        
        List<NotificationMessage.OrderItemInfo> orderItems = order.getOrderItems() != null ?
                order.getOrderItems().stream()
                        .map(item -> NotificationMessage.OrderItemInfo.builder()
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()) :
                List.of();
        
        String subject = getSubjectForNotificationType(notificationType, order);
        String message = getMessageForNotificationType(notificationType, order);
        
        // For FLORIST notification type, recipientEmail is the florist email
        // For other types, recipientEmail is the customer email
        String floristEmailForMessage = "FLORIST".equals(notificationType) ? 
                recipientEmail : (customer != null ? customer.getEmail() : null);
        
        return NotificationMessage.builder()
                .notificationType(notificationType)
                .orderNumber(order.getOrderNumber())
                .orderId(order.getId())
                .customerEmail(customer != null ? customer.getEmail() : null)
                .customerUsername(customer != null ? customer.getUsername() : null)
                .floristEmail(floristEmailForMessage)
                .subject(subject)
                .message(message)
                .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .orderDate(order.getCreatedAt())
                .orderItems(orderItems)
                .notes(order.getNotes())
                .build();
    }
    
    private String getTopicForNotificationType(String notificationType) {
        return switch (notificationType) {
            case "FLORIST" -> KafkaConfig.FLORIST_NOTIFICATION_TOPIC;
            case "STATUS_UPDATE" -> KafkaConfig.ORDER_STATUS_TOPIC;
            default -> KafkaConfig.NOTIFICATION_TOPIC;
        };
    }
    
    private String getSubjectForNotificationType(String notificationType, Order order) {
        return switch (notificationType) {
            case "FLORIST" -> "New Order Confirmed - " + order.getOrderNumber();
            case "STATUS_UPDATE" -> "Order Status Update - " + order.getOrderNumber();
            default -> "Order Confirmed - " + order.getOrderNumber();
        };
    }
    
    private String getMessageForNotificationType(String notificationType, Order order) {
        return switch (notificationType) {
            case "FLORIST" -> "A new order has been confirmed and requires your attention.";
            case "STATUS_UPDATE" -> "Your order status has been updated to: " + order.getStatus();
            default -> "Your order has been confirmed successfully!";
        };
    }
    
    private void sendEmailFallback(Order order, String notificationType, String recipientEmail) {
        try {
            String subject = getSubjectForNotificationType(notificationType, order);
            String content = switch (notificationType) {
                case "FLORIST" -> buildFloristEmailContent(order);
                case "STATUS_UPDATE" -> buildOrderStatusUpdateEmailContent(order);
                default -> buildCustomerEmailContent(order);
            };
            
            sendHtmlEmail(recipientEmail, subject, content);
            log.info("Email fallback sent successfully for order: {} to email: {}", 
                    order.getOrderNumber(), recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send email fallback for order: {} to email: {}", 
                    order.getOrderNumber(), recipientEmail, e);
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
