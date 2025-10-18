# Notification System Implementation Guide

## Overview
This document describes the comprehensive notification system implemented for the OrderFlow application. The system provides email notifications for order confirmations, status updates, and florist alerts.

## Features Implemented

### 1. Email Notifications
- **Florist Notifications**: Sent when a new order is confirmed
- **Customer Notifications**: Sent when order is confirmed and when status changes
- **HTML Email Templates**: Professional-looking email templates with order details
- **Configurable Email Settings**: Easy configuration through application.yaml

### 2. Notification Types

#### Florist Notification
- Triggered when a COD order is confirmed
- Contains complete order details including:
  - Order number and customer information
  - Total amount and payment method
  - Shipping address and delivery notes
  - Complete list of ordered items with quantities and prices
  - Order date and time

#### Customer Confirmation Notification
- Sent immediately after order confirmation
- Includes order summary and payment information
- Special messaging for COD orders
- Professional confirmation message

#### Order Status Update Notification
- Sent when order status changes (SHIPPED, DELIVERED, CANCELLED)
- Status-specific messaging
- Updated order information

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
    void sendFloristNotification(Order order);
    void sendCustomerNotification(Order order);
    void sendOrderStatusUpdateNotification(Order order);
}
```

#### NotificationServiceImpl
- Handles email template generation
- Manages email sending with error handling
- Configurable notification enable/disable
- HTML email support with professional styling

### 3. Configuration

#### Application.yaml Configuration
```yaml
# Email Configuration
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
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
      from: ${MAIL_FROM:noreply@orderflow.com}
      florist: ${FLORIST_EMAIL:florist@orderflow.com}
```

#### Environment Variables
- `MAIL_USERNAME`: Your email username
- `MAIL_PASSWORD`: Your email app password
- `MAIL_FROM`: From email address
- `FLORIST_EMAIL`: Florist notification email

### 4. Database Changes

#### User Entity Update
Added email field to User entity:
```java
@Column(name = "email")
private String email;
```

**Database Migration Required:**
```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255);
```

## API Endpoints

### Order Status Update (Florist Only)
```
PUT /api/orders/{orderId}/status?status={ORDER_STATUS}
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
PUT /api/orders/1/status?status=SHIPPED
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Email Templates

### Florist Notification Template
- Professional HTML layout
- Complete order information table
- Order items with quantities and prices
- Action required message
- Responsive design

### Customer Notification Template
- Welcome message with customer name
- Order confirmation details
- Payment method information
- Order items summary
- Delivery information
- Professional footer

### Status Update Template
- Status-specific messaging
- Updated order information
- Professional styling
- Clear status indication

## Setup Instructions

### 1. Gmail Setup (Recommended)
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
3. Use the app password in `MAIL_PASSWORD` environment variable

### 2. Environment Variables
Create a `.env` file or set environment variables:
```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-16-character-app-password
export MAIL_FROM=noreply@orderflow.com
export FLORIST_EMAIL=florist@orderflow.com
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
4. Check email notifications

## Error Handling

### Notification Service Error Handling
- Graceful failure with logging
- No impact on order processing
- Configurable enable/disable
- Email validation before sending

### Common Issues and Solutions

#### Email Not Sending
1. Check email configuration in application.yaml
2. Verify environment variables
3. Check Gmail app password
4. Review application logs

#### User Email Not Found
- Notification is skipped with warning log
- Order processing continues normally
- Consider adding email validation during user registration

## Security Considerations

### Email Security
- Use app passwords instead of account passwords
- Enable 2FA on email accounts
- Use environment variables for sensitive data
- Consider using dedicated email service for production

### Access Control
- Only florists can update order status
- Customer notifications only sent to order owner
- Proper authentication required for all endpoints

## Monitoring and Logging

### Log Messages
- Notification sending attempts
- Success/failure status
- Email addresses (masked in production)
- Order numbers and status changes

### Monitoring Points
- Email delivery success rate
- Notification service performance
- Failed notification attempts
- Order status update frequency

## Future Enhancements

### Planned Features
1. **SMS Notifications**: Add SMS support for critical updates
2. **Push Notifications**: Mobile app integration
3. **Email Templates**: External template management
4. **Notification Preferences**: User-configurable notification settings
5. **Delivery Tracking**: Integration with shipping providers
6. **Multi-language Support**: Localized email templates

### Technical Improvements
1. **Async Processing**: Queue-based notification processing
2. **Retry Logic**: Automatic retry for failed notifications
3. **Template Engine**: Thymeleaf or similar for dynamic templates
4. **Notification History**: Track all sent notifications
5. **Analytics**: Notification open/click tracking

## Testing

### Unit Tests
- NotificationService methods
- Email template generation
- Error handling scenarios

### Integration Tests
- End-to-end order flow with notifications
- Email sending with test configuration
- Status update notifications

### Manual Testing
1. Create test orders with different statuses
2. Verify email content and formatting
3. Test error scenarios (invalid email, network issues)
4. Validate notification timing

## Troubleshooting

### Common Issues
1. **Emails not received**: Check spam folder, verify email configuration
2. **Template formatting issues**: Review HTML template code
3. **Performance issues**: Consider async processing for high volume
4. **Configuration errors**: Validate application.yaml syntax

### Debug Mode
Enable debug logging for email:
```yaml
logging:
  level:
    com.SWD_G4.OrderFlow.service.impl.NotificationServiceImpl: DEBUG
    org.springframework.mail: DEBUG
```

## Conclusion

The notification system provides a comprehensive solution for order-related communications. It's designed to be:
- **Reliable**: Graceful error handling and logging
- **Configurable**: Easy setup and customization
- **Professional**: High-quality email templates
- **Scalable**: Ready for future enhancements
- **Secure**: Proper authentication and data protection

The system successfully completes the COD flow implementation by providing the missing notification functionality for both florists and customers.
