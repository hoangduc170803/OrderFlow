# Cash on Delivery (COD) Flow Implementation

## Tổng quan
Đây là implementation của luồng Cash on Delivery (COD) theo activity diagram đã cung cấp. Luồng này cho phép khách hàng đặt hàng và thanh toán khi nhận hàng.

## Luồng COD được implement

### 1. Customer Login & View Products
- **Endpoint**: `GET /api/products` (public)
- **Endpoint**: `GET /api/products/{productId}` (public)
- Khách hàng có thể xem danh sách sản phẩm và chi tiết sản phẩm mà không cần đăng nhập

### 2. Add to Cart
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

### 3. View Cart
- **Endpoint**: `GET /api/cart`
- Xem giỏ hàng hiện tại với tổng tiền

### 4. Update/Remove Cart Items
- **Update**: `PUT /api/cart/items/{cartItemId}`
- **Remove**: `DELETE /api/cart/items/{cartItemId}`
- **Clear Cart**: `DELETE /api/cart/clear`

### 5. Proceed to Checkout (Create Order)
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

### 6. Confirm COD Order
- **Endpoint**: `POST /api/orders/{orderId}/confirm-cod`
- Khách hàng xác nhận đơn hàng COD trên website
- Update order status từ PENDING → CONFIRMED
- Decrement product inventory
- Gửi notification cho florist (TODO: implement notification service)
- Gửi confirmation cho customer (TODO: implement notification service)

### 7. View Orders
- **Get Single Order**: `GET /api/orders/{orderId}`
- **Get User Orders**: `GET /api/orders`

## Entities được tạo

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

## Services

### CartService
- `getCart()`: Lấy giỏ hàng của user
- `addToCart()`: Thêm sản phẩm vào giỏ hàng
- `updateCartItem()`: Cập nhật số lượng sản phẩm
- `removeFromCart()`: Xóa sản phẩm khỏi giỏ hàng
- `clearCart()`: Xóa toàn bộ giỏ hàng

### OrderService
- `createOrder()`: Tạo đơn hàng mới
- `confirmCODOrder()`: Xác nhận đơn hàng COD
- `getOrder()`: Lấy thông tin đơn hàng
- `getUserOrders()`: Lấy danh sách đơn hàng của user

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

## Database Changes
Cần tạo các bảng mới:
- `carts`: Lưu thông tin giỏ hàng
- `cart_items`: Lưu chi tiết sản phẩm trong giỏ hàng

## API Testing Examples

### 1. Xem sản phẩm (không cần đăng nhập)
```bash
GET /api/products
GET /api/products/1
```

### 2. Đăng nhập và lấy token
```bash
POST /auth/token
{
    "username": "customer1",
    "password": "password123"
}
```

### 3. Thêm sản phẩm vào giỏ hàng
```bash
POST /api/cart/add
Authorization: Bearer {token}
{
    "productId": 1,
    "quantity": 2
}
```

### 4. Xem giỏ hàng
```bash
GET /api/cart
Authorization: Bearer {token}
```

### 5. Tạo đơn hàng COD
```bash
POST /api/orders
Authorization: Bearer {token}
{
    "shippingAddress": "123 Main St, Ho Chi Minh City",
    "notes": "Please deliver in the morning",
    "paymentMethod": "COD"
}
```

### 6. Xác nhận đơn hàng COD
```bash
POST /api/orders/{orderId}/confirm-cod
Authorization: Bearer {token}
```

## Luồng hoàn chỉnh theo Activity Diagram

1. ✅ **Customer Login** - Authentication service đã có
2. ✅ **View Flower** - ProductController với public endpoints
3. ✅ **Add to Cart** - CartService.addToCart()
4. ✅ **Proceeds to Checkout** - OrderService.createOrder()
5. ✅ **Provide Information** - CreateOrderRequest với shippingAddress
6. ✅ **Selects Payment Method** - PaymentMethod.COD
7. ✅ **Payment Method is COD?** - Logic trong OrderService
8. ✅ **Confirms the order directly on the site** - OrderController.confirmCODOrder()
9. ✅ **Updates order status to "Confirmed"** - OrderService.confirmCODOrder()
10. ✅ **Triggers "New Order" notification for the Florist** - TODO: Notification service
11. ✅ **Notifies Customer of the successful order** - TODO: Notification service
12. ✅ **Decrements product inventory** - OrderService.decrementProductInventory()

## TODO Items
- [ ] Implement notification service cho florist và customer
- [ ] Add email/SMS notifications
- [ ] Add order status tracking
- [ ] Add order history và analytics
- [ ] Add inventory management features
- [ ] Add order cancellation functionality
