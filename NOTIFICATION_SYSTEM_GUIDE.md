# Notification System Implementation Guide

## Overview
This document describes the comprehensive notification system implemented for the OrderFlow application. The system provides HTML email notifications for order confirmations, status updates, and florist alerts using Spring Boot Mail integration with Gmail SMTP.

## Features Implemented

### 1. Email Notifications
- **Florist Notifications**: Sent when a new order is confirmed
- **Customer Notifications**: Sent when order is confirmed and when status changes
- **HTML Email Templates**: Professional-looking email templates with order details
- **Configurable Email Settings**: Easy configuration through application.yaml
- **Error Handling**: Graceful failure with comprehensive logging
- **Email Validation**: Checks for valid email addresses before sending

### 2. Notification Types

#### Florist Notification
- Triggered when a COD order is confirmed
- Contains complete order details including:
  - Order number and customer information
  - Total amount and payment method
  - Shipping address and delivery notes
  - Complete list of ordered items with quantities and prices
  - Order date and time
  - Action required message for florist

#### Customer Confirmation Notification
- Sent immediately after order confirmation
- Includes order summary and payment information
- Special messaging for COD orders
- Professional confirmation message with order details
- Order items summary with pricing

#### Order Status Update Notification
- Sent when order status changes (SHIPPED, DELIVERED, CANCELLED)
- Status-specific messaging
- Updated order information
- Professional styling with clear status indication

## Technical Implementation

### 1. Service Architecture
```
NotificationService (Interface)
    ↓
NotificationServiceImpl (Implementation)
    ↓
JavaMailSender (Spring Boot Mail)
```

### 2. Key Components

#### NotificationService Interface
```java
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
```

#### NotificationServiceImpl Features
- **HTML Email Support**: Professional email templates with tables and styling
- **Error Handling**: Comprehensive exception handling with logging
- **Configurable**: Enable/disable notifications via configuration
- **Email Validation**: Checks for valid email addresses before sending
- **Logging**: Detailed logging for monitoring and debugging
- **UTF-8 Encoding**: Proper character encoding for international content

#### EmailConfig Configuration
```java
@Configuration
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String mailHost;
    
    @Value("${spring.mail.port}")
    private int mailPort;
    
    @Value("${spring.mail.username}")
    private String mailUsername;
    
    @Value("${spring.mail.password}")
    private String mailPassword;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", mailHost);
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}
```

### 3. Configuration

#### Application.yaml Configuration
```yaml
# Email Configuration
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: smtp.gmail.com

# Notification Configuration
app:
  notification:
    enabled: true
    email:
      from: your-email@gmail.com      # ← Email người gửi
      florist: florist@orderflow.com  # ← Email chủ shop nhận thông báo
```

#### Environment Variables (Optional)
- `MAIL_USERNAME`: Your email username
- `MAIL_PASSWORD`: Your email app password
- `MAIL_FROM`: From email address
- `FLORIST_EMAIL`: Florist notification email

### 4. Database Requirements

#### User Entity Update
The User entity includes an email field:
```java
@Column(name = "email")
private String email;
```

**Database Migration Required:**
```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
```

## Email Templates

### Florist Notification Template Features
- **Professional HTML Layout**: Clean, readable design
- **Complete Order Information Table**: All order details in structured format
- **Order Items Table**: Detailed product list with quantities and prices
- **Action Required Message**: Clear instructions for florist
- **Responsive Design**: Works on different email clients
- **Order Details Include**:
  - Order number and customer name
  - Total amount and payment method
  - Shipping address and notes
  - Order date and time
  - Complete itemized list

### Customer Notification Template Features
- **Welcome Message**: Personalized greeting with customer name
- **Order Confirmation Details**: Complete order summary
- **Payment Information**: Clear payment method details
- **Order Items Summary**: Itemized list with pricing
- **Delivery Information**: Shipping address and special instructions
- **COD Special Handling**: Special messaging for Cash on Delivery orders
- **Professional Footer**: Branded closing message

### Status Update Template Features
- **Status-Specific Messaging**: Different messages for each status
- **Updated Order Information**: Current order details
- **Professional Styling**: Consistent with other templates
- **Clear Status Indication**: Easy to understand status changes
- **Status Messages Include**:
  - SHIPPED: "Your order has been shipped and is on its way!"
  - DELIVERED: "Your order has been delivered successfully!"
  - CANCELLED: "Your order has been cancelled"

## Implementation Details

### NotificationServiceImpl Key Methods

#### sendFloristNotification()
```java
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
```

#### sendCustomerNotification()
```java
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
```

#### sendHtmlEmail()
```java
private void sendHtmlEmail(String to, String subject, String content) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    
    helper.setFrom(fromEmail);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(content, true); // true indicates HTML content
    
    mailSender.send(message);
}
```

## Setup Instructions

### 1. Gmail Setup (Recommended)
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate an App Password**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
3. **Use the app password** in your application configuration

### 2. Configuration Setup
Update your `application.yaml` with your email credentials:
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-16-character-app-password

app:
  notification:
    enabled: true
    email:
      from: your-email@gmail.com
      florist: florist@orderflow.com
```

### 3. Database Migration
Run the SQL command to add email column to users table:
```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
```

### 4. Testing
1. Create a test user with email address
2. Place a COD order
3. Confirm the order
4. Check email notifications (including spam folder)

## Integration Points

### Order Service Integration
The notification service is integrated with the OrderService:
```java
@Service
public class OrderService {
    @Autowired
    private NotificationService notificationService;
    
    public Order confirmOrder(Long orderId) {
        // Order confirmation logic
        Order order = // ... confirmation process
        
        // Send notifications
        notificationService.sendFloristNotification(order);
        notificationService.sendCustomerNotification(order);
        
        return order;
    }
    
    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        // Status update logic
        Order order = // ... status update process
        
        // Send status update notification
        notificationService.sendOrderStatusUpdateNotification(order);
        
        return order;
    }
}
```

### Controller Integration
Status updates are handled through REST endpoints:
```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status,
            Authentication authentication) {
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(orderMapper.toResponse(updatedOrder));
    }
}
```

## Error Handling

### Notification Service Error Handling
- **Graceful Failure**: Notifications don't affect order processing
- **Comprehensive Logging**: All attempts and failures are logged
- **Email Validation**: Checks for valid email addresses
- **Configurable Enable/Disable**: Can be turned off via configuration
- **Exception Handling**: Catches and logs all email-related exceptions

### Common Issues and Solutions

#### Email Not Sending
1. **Check Configuration**: Verify email settings in application.yaml
2. **Verify Credentials**: Ensure email username and password are correct
3. **Check Gmail App Password**: Verify 16-character app password
4. **Review Logs**: Check application logs for detailed error messages
5. **Network Issues**: Ensure SMTP port 587 is accessible

#### User Email Not Found
- **Graceful Handling**: Notification is skipped with warning log
- **Order Processing Continues**: Order confirmation proceeds normally
- **Recommendation**: Add email validation during user registration

#### Template Rendering Issues
- **HTML Validation**: Check HTML template syntax
- **Character Encoding**: Ensure UTF-8 encoding for special characters
- **Email Client Compatibility**: Test across different email clients

## Security Considerations

### Email Security
- **App Passwords**: Use app passwords instead of account passwords
- **2FA Required**: Enable 2-factor authentication on email accounts
- **Environment Variables**: Use environment variables for sensitive data
- **Production Email Service**: Consider dedicated email service for production
- **SSL/TLS**: Always use encrypted connections (port 587 with STARTTLS)

### Access Control
- **Florist-Only Updates**: Only florists can update order status
- **Customer Privacy**: Customer notifications only sent to order owner
- **Authentication Required**: All endpoints require proper authentication
- **Email Validation**: Validate email addresses before sending

## Monitoring and Logging

### Log Messages
- **Notification Attempts**: All notification sending attempts are logged
- **Success/Failure Status**: Clear indication of notification success
- **Email Addresses**: Logged (masked in production for security)
- **Order Numbers**: All notifications include order number for tracking
- **Status Changes**: Order status updates are logged

### Monitoring Points
- **Email Delivery Success Rate**: Track successful vs failed notifications
- **Notification Service Performance**: Monitor processing time
- **Failed Notification Attempts**: Track and alert on failures
- **Order Status Update Frequency**: Monitor business activity

### Debug Mode
Enable debug logging for detailed email information:
```yaml
logging:
  level:
    com.SWD_G4.OrderFlow.service.impl.NotificationServiceImpl: DEBUG
    org.springframework.mail: DEBUG
```

## API Usage Examples

### Order Status Update (Florist Only)
```
PUT /order_flow/api/orders/{orderId}/status?status={ORDER_STATUS}
Authorization: Bearer {token}
```

**Order Status Values:**
- `PENDING`
- `CONFIRMED`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

**Example:**
```bash
curl -X PUT "http://localhost:8080/order_flow/api/orders/1/status?status=SHIPPED" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response:**
```json
{
  "orderNumber": "ORD-2025-001",
  "status": "SHIPPED",
  "totalAmount": 150.00,
  "customerName": "John Doe",
  "updatedAt": "2025-01-19T10:30:00"
}
```

## Testing

### Unit Tests
```java
@Test
public void testSendFloristNotification() {
    Order order = createTestOrder();
    notificationService.sendFloristNotification(order);
    
    // Verify email was sent (mock verification)
    verify(mailSender).send(any(MimeMessage.class));
}

@Test
public void testSendCustomerNotification_WithValidEmail() {
    Order order = createTestOrderWithEmail();
    notificationService.sendCustomerNotification(order);
    
    // Verify notification was sent
    verify(mailSender).send(any(MimeMessage.class));
}

@Test
public void testSendCustomerNotification_WithoutEmail() {
    Order order = createTestOrderWithoutEmail();
    notificationService.sendCustomerNotification(order);
    
    // Verify no email was sent
    verify(mailSender, never()).send(any(MimeMessage.class));
}
```

### Integration Tests
```java
@Test
@SpringBootTest
public class NotificationIntegrationTest {
    
    @Test
    public void testOrderConfirmationFlow() {
        // Create user with email
        User user = createUserWithEmail();
        
        // Create and confirm order
        Order order = orderService.confirmOrder(createOrderRequest());
        
        // Verify notifications were sent
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        // Additional assertions for notification content
    }
}
```

### Manual Testing Checklist
1. **Email Configuration**: Verify email settings work
2. **Order Confirmation**: Test complete order flow
3. **Status Updates**: Test all status change scenarios
4. **Email Content**: Verify template formatting and content
5. **Error Scenarios**: Test invalid emails, network issues
6. **Performance**: Test under load conditions

## Troubleshooting

### Common Issues
1. **Emails not received**: 
   - Check spam folder
   - Verify email configuration
   - Check Gmail app password
   - Review application logs

2. **Template formatting issues**: 
   - Review HTML template code
   - Test across different email clients
   - Check character encoding

3. **Performance issues**: 
   - Monitor email sending times
   - Consider async processing for high volume
   - Check SMTP server response times

4. **Configuration errors**: 
   - Validate application.yaml syntax
   - Check environment variable values
   - Verify email credentials

### Debug Steps
1. **Enable Debug Logging**: Set log level to DEBUG
2. **Check Application Logs**: Review detailed error messages
3. **Test Email Configuration**: Use simple test email first
4. **Verify Database**: Ensure user emails are properly stored
5. **Network Connectivity**: Test SMTP connection manually

## Future Enhancements

### Planned Features
1. **SMS Notifications**: Add SMS support for critical updates
2. **Push Notifications**: Mobile app integration
3. **Email Templates**: External template management system
4. **Notification Preferences**: User-configurable notification settings
5. **Delivery Tracking**: Integration with shipping providers
6. **Multi-language Support**: Localized email templates
7. **Email Scheduling**: Delayed notification sending
8. **Notification History**: Track all sent notifications

### Technical Improvements
1. **Async Processing**: Queue-based notification processing
2. **Retry Logic**: Automatic retry for failed notifications
3. **Template Engine**: Thymeleaf integration for dynamic templates
4. **Notification Analytics**: Open/click tracking
5. **Bulk Notifications**: Efficient batch email sending
6. **Email Validation**: Advanced email format validation
7. **Rate Limiting**: Prevent email spam and abuse
8. **Email Testing**: Automated email template testing

## Conclusion

The notification system provides a comprehensive solution for order-related communications in the OrderFlow application. Key features include:

### ✅ **Implemented Features**
- **Professional HTML Email Templates**: Well-formatted, responsive email designs
- **Multiple Notification Types**: Florist, customer, and status update notifications
- **Robust Error Handling**: Graceful failure with comprehensive logging
- **Easy Configuration**: Simple setup through application.yaml
- **Security Best Practices**: App passwords, SSL/TLS, proper authentication
- **Integration Ready**: Seamlessly integrated with order processing flow

### 🎯 **Key Benefits**
- **Improved Customer Experience**: Timely, professional communications
- **Florist Efficiency**: Automated order notifications reduce manual work
- **Reliability**: Comprehensive error handling ensures system stability
- **Scalability**: Ready for future enhancements and high-volume usage
- **Maintainability**: Clean code structure with proper separation of concerns

### 📈 **Business Impact**
- **Reduced Manual Work**: Automated notifications save time
- **Better Communication**: Customers stay informed about order status
- **Professional Image**: High-quality email templates enhance brand perception
- **Operational Efficiency**: Florists receive timely order notifications
- **Customer Satisfaction**: Clear, timely communication improves user experience

The notification system successfully completes the OrderFlow application by providing essential communication functionality that enhances both customer experience and operational efficiency.