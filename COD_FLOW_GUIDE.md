# Cash on Delivery (COD) Flow Implementation

## Tổng quan
Đây là implementation của luồng Cash on Delivery (COD) theo activity diagram đã cung cấp. Luồng này cho phép khách hàng đặt hàng và thanh toán khi nhận hàng, với hệ thống notification đầy đủ.

## Luồng COD được implement

### 1. User Registration & Login
- **User Registration**: `POST /users`
- **Request Body**:
```json
{
    "username": "customer1",
    "password": "password123",
    "email": "customer@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
}
```
- **Authentication**: `POST /auth/token`
- User được tự động gán role CUSTOMER
- Email được yêu cầu để nhận notifications

### 2. Customer View Products
- **Endpoint**: `GET /api/products` (public)
- **Endpoint**: `GET /api/products/{productId}` (public)
- Khách hàng có thể xem danh sách sản phẩm và chi tiết sản phẩm mà không cần đăng nhập

### 3. Add to Cart
- **Endpoint**: `POST /api/cart/add`
- **Request Body**:
```json
{
    "productId": 1,
    "quantity": 2
}
```
- Thêm sản phẩm vào giỏ hàng
- Kiểm tra stock availability
- Tự động tạo giỏ hàng nếu chưa có

### 4. View Cart
- **Endpoint**: `GET /api/cart`
- Xem giỏ hàng hiện tại với tổng tiền

### 5. Update/Remove Cart Items
- **Update**: `PUT /api/cart/items/{cartItemId}`
- **Remove**: `DELETE /api/cart/items/{cartItemId}`
- **Clear Cart**: `DELETE /api/cart/clear`

### 6. Proceed to Checkout (Create Order)
- **Endpoint**: `POST /api/orders`
- **Request Body**:
```json
{
    "shippingAddress": "123 Main St, Ho Chi Minh City",
    "notes": "Please deliver in the morning",
    "paymentMethod": "COD"
}
```
- Validate stock cho tất cả items trong giỏ hàng
- Tạo order với status PENDING
- Generate unique order number
- Clear giỏ hàng sau khi tạo order thành công

### 7. Confirm COD Order
- **Endpoint**: `POST /api/orders/{orderId}/confirm-cod`
- Khách hàng xác nhận đơn hàng COD trên website
- Update order status từ PENDING → CONFIRMED
- Decrement product inventory
- ✅ **Gửi notification cho florist** - NotificationService.sendFloristNotification()
- ✅ **Gửi confirmation cho customer** - NotificationService.sendCustomerNotification()

### 8. Order Status Updates (Florist Only)
- **Endpoint**: `PUT /api/orders/{orderId}/status?status={ORDER_STATUS}`
- **Available Status**: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
- ✅ **Gửi status update notification cho customer** - NotificationService.sendOrderStatusUpdateNotification()

### 9. View Orders
- **Get Single Order**: `GET /api/orders/{orderId}`
- **Get User Orders**: `GET /api/orders`

## Entities được tạo

### User Entity (Updated)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")  // ✅ Added for notifications
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @ManyToMany
    @JoinTable(name = "user_role", ...)
    private Set<Role> roles;
}
```

### Cart & CartItem
- `Cart`: Quản lý giỏ hàng của user
- `CartItem`: Chi tiết sản phẩm trong giỏ hàng

### PaymentMethod Enum
```java
public enum PaymentMethod {
    COD, // Cash on Delivery
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET
}
```

### Order Entity
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

## Services

### CartService
- `getCart()`: Lấy giỏ hàng của user
- `addToCart()`: Thêm sản phẩm vào giỏ hàng
- `updateCartItem()`: Cập nhật số lượng sản phẩm
- `removeFromCart()`: Xóa sản phẩm khỏi giỏ hàng
- `clearCart()`: Xóa toàn bộ giỏ hàng

### OrderService
- `createOrder()`: Tạo đơn hàng mới
- `confirmCODOrder()`: Xác nhận đơn hàng COD với notifications
- `updateOrderStatus()`: Cập nhật trạng thái đơn hàng với notifications
- `getOrder()`: Lấy thông tin đơn hàng
- `getUserOrders()`: Lấy danh sách đơn hàng của user

### NotificationService ✅ (Implemented)
- `sendFloristNotification()`: Gửi email thông báo cho florist khi có đơn hàng mới
- `sendCustomerNotification()`: Gửi email xác nhận đơn hàng cho khách hàng
- `sendOrderStatusUpdateNotification()`: Gửi email cập nhật trạng thái đơn hàng

## Email Notification System

### Florist Notification
- **Trigger**: Khi đơn hàng COD được xác nhận
- **Content**: 
  - Order number và customer information
  - Total amount và payment method
  - Shipping address và delivery notes
  - Complete list of ordered items
  - Action required message

### Customer Confirmation Notification
- **Trigger**: Khi đơn hàng được xác nhận
- **Content**:
  - Order confirmation details
  - Payment method information (COD)
  - Order items summary
  - Delivery information

### Order Status Update Notification
- **Trigger**: Khi florist cập nhật trạng thái đơn hàng
- **Status Messages**:
  - SHIPPED: "Your order has been shipped and is on its way!"
  - DELIVERED: "Your order has been delivered successfully!"
  - CANCELLED: "Your order has been cancelled"

## Error Handling
Đã thêm các error codes mới:
- `PRODUCT_NOT_FOUND` (2001)
- `PRODUCT_NOT_AVAILABLE` (2002)
- `INSUFFICIENT_STOCK` (2003)
- `CART_NOT_FOUND` (3001)
- `CART_ITEM_NOT_FOUND` (3002)
- `ORDER_NOT_FOUND` (4001)
- `ORDER_ALREADY_CONFIRMED` (4002)
- `EMPTY_CART` (4003)

## Security
- Tất cả endpoints (trừ xem sản phẩm) yêu cầu authentication
- User chỉ có thể truy cập giỏ hàng và đơn hàng của chính mình
- Products endpoints được public để khách hàng có thể xem trước khi đăng nhập
- Chỉ florist có thể cập nhật trạng thái đơn hàng

## Database Changes
Cần tạo các bảng mới:
- `carts`: Lưu thông tin giỏ hàng
- `cart_items`: Lưu chi tiết sản phẩm trong giỏ hàng
- `users`: ✅ Thêm cột `email` cho notifications
- `orders`: Lưu thông tin đơn hàng
- `order_items`: Lưu chi tiết sản phẩm trong đơn hàng

## Email Configuration
```yaml
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

app:
  notification:
    enabled: true
    email:
      from: your-email@gmail.com
      florist: florist@orderflow.com
```

## API Testing Examples

### 1. User Registration
```bash
POST /users
{
    "username": "customer1",
    "password": "password123",
    "email": "customer@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
}
```

### 2. User Login
```bash
POST /auth/token
{
    "username": "customer1",
    "password": "password123"
}
```

### 3. Xem sản phẩm (không cần đăng nhập)
```bash
GET /api/products
GET /api/products/1
```

### 4. Thêm sản phẩm vào giỏ hàng
```bash
POST /api/cart/add
Authorization: Bearer {token}
{
    "productId": 1,
    "quantity": 2
}
```

### 5. Xem giỏ hàng
```bash
GET /api/cart
Authorization: Bearer {token}
```

### 6. Tạo đơn hàng COD
```bash
POST /api/orders
Authorization: Bearer {token}
{
    "shippingAddress": "123 Main St, Ho Chi Minh City",
    "notes": "Please deliver in the morning",
    "paymentMethod": "COD"
}
```

### 7. Xác nhận đơn hàng COD
```bash
POST /api/orders/{orderId}/confirm-cod
Authorization: Bearer {token}
```

### 8. Cập nhật trạng thái đơn hàng (Florist Only)
```bash
PUT /api/orders/{orderId}/status?status=SHIPPED
Authorization: Bearer {florist_token}
```

## Luồng hoàn chỉnh theo Activity Diagram

1. ✅ **Customer Registration** - UserController với email field
2. ✅ **Customer Login** - Authentication service đã có
3. ✅ **View Flower** - ProductController với public endpoints
4. ✅ **Add to Cart** - CartService.addToCart()
5. ✅ **Proceeds to Checkout** - OrderService.createOrder()
6. ✅ **Provide Information** - CreateOrderRequest với shippingAddress
7. ✅ **Selects Payment Method** - PaymentMethod.COD
8. ✅ **Payment Method is COD?** - Logic trong OrderService
9. ✅ **Confirms the order directly on the site** - OrderController.confirmCODOrder()
10. ✅ **Updates order status to "Confirmed"** - OrderService.confirmCODOrder()
11. ✅ **Triggers "New Order" notification for the Florist** - NotificationService.sendFloristNotification()
12. ✅ **Notifies Customer of the successful order** - NotificationService.sendCustomerNotification()
13. ✅ **Decrements product inventory** - OrderService.decrementProductInventory()
14. ✅ **Florist updates order status** - OrderController.updateOrderStatus()
15. ✅ **Customer receives status update notification** - NotificationService.sendOrderStatusUpdateNotification()

## Notification Flow

### Order Confirmation Flow
1. Customer confirms COD order
2. Order status: PENDING → CONFIRMED
3. Product inventory decremented
4. Email sent to florist with order details
5. Email sent to customer with confirmation

### Order Status Update Flow
1. Florist updates order status (SHIPPED/DELIVERED/CANCELLED)
2. Order status updated in database
3. Email sent to customer with status-specific message
4. Customer informed of order progress

## Email Templates

### Florist Notification Template
- Professional HTML layout
- Complete order information table
- Order items with quantities and prices
- Action required message for florist

### Customer Notification Templates
- **Confirmation**: Order confirmation with COD payment info
- **Status Updates**: Status-specific messages (shipped, delivered, cancelled)
- Professional styling with order details

## Setup Instructions

### 1. Gmail Setup
1. Enable 2-Factor Authentication
2. Generate App Password
3. Configure email settings in application.yaml

### 2. Database Setup
```sql
-- Add email column to users table
ALTER TABLE users ADD COLUMN email VARCHAR(255);

-- Create other required tables (carts, cart_items, orders, order_items)
-- See ENTITY_GUIDE.md for complete schema
```

### 3. Testing
1. Create user with email address
2. Place COD order
3. Confirm order
4. Check email notifications
5. Update order status as florist
6. Verify status update notifications

## Completed Features ✅

- ✅ User registration with email field
- ✅ Complete COD order flow
- ✅ Cart management system
- ✅ Order creation and confirmation
- ✅ Email notification system
- ✅ Order status updates
- ✅ Florist and customer notifications
- ✅ Inventory management
- ✅ Security and authentication
- ✅ Error handling and validation

## Future Enhancements

- [ ] SMS notifications for critical updates
- [ ] Push notifications for mobile app
- [ ] Order tracking with delivery updates
- [ ] Customer order history and analytics
- [ ] Advanced inventory management
- [ ] Order cancellation with refunds
- [ ] Multi-language email templates
- [ ] Email template customization
- [ ] Notification preferences per user

## Conclusion

The COD flow implementation is now complete with:
- ✅ **Full User Management**: Registration with email for notifications
- ✅ **Complete Order Flow**: From cart to delivery
- ✅ **Notification System**: Email notifications for all stakeholders
- ✅ **Status Tracking**: Real-time order status updates
- ✅ **Professional Communication**: HTML email templates
- ✅ **Security**: Role-based access control
- ✅ **Error Handling**: Comprehensive error management

The system successfully implements the complete COD workflow with professional email notifications, ensuring all stakeholders are properly informed throughout the order lifecycle.