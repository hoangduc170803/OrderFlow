# OrderFlow - Spring Boot E-commerce Application

A comprehensive e-commerce application built with Spring Boot, featuring JWT authentication, cart management, product catalog, order processing, and email notifications with Cash on Delivery (COD) support.

## 🌟 Features

### Core Features
- 🔐 **JWT Authentication & Authorization** with role-based access control
- 🛒 **Shopping Cart Management** with real-time calculations
- 📦 **Product Catalog** with category management
- 👥 **User Management** with email notifications
- 📋 **Order Processing** with status tracking
- 💰 **Cash on Delivery (COD)** payment method
- 📧 **Email Notification System** for order updates
- 🏷️ **Category Management** for product organization
- 🔒 **Role-based Access Control** (Customer, Florist, Admin)

### Advanced Features
- 📱 **RESTful API** with comprehensive endpoints
- 🗄️ **Database Auditing** with automatic timestamps
- ⚡ **Performance Optimized** with lazy loading
- 🛡️ **Security** with JWT tokens and password encryption
- 📊 **Order Status Tracking** (Pending → Confirmed → Shipped → Delivered)
- 📧 **HTML Email Templates** for professional communications
- 🔄 **Real-time Inventory Management**

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.6
- **Security**: Spring Security with JWT
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA with Hibernate
- **Email**: Spring Boot Mail with Gmail SMTP
- **Build Tool**: Maven
- **Java Version**: 17+
- **Validation**: Bean Validation (JSR-303)
- **Logging**: SLF4J with Logback

## 📚 Documentation

- **[Entity Guide](ENTITY_GUIDE.md)** - Complete entity structure and relationships
- **[COD Flow Guide](COD_FLOW_GUIDE.md)** - Cash on Delivery implementation
- **[Notification System Guide](NOTIFICATION_SYSTEM_GUIDE.md)** - Email notification system
- **[Test Collection](COD_Test_Collection.json)** - Postman collection for API testing

## 🚀 API Endpoints

### Authentication
- `POST /order_flow/auth/token` - Login
- `POST /order_flow/auth/refresh` - Refresh token
- `POST /order_flow/auth/logout` - Logout
- `POST /order_flow/auth/introspect` - Token introspection

### User Management
- `POST /order_flow/users` - Create user account (with email)
- User registration includes: username, password, email, firstName, lastName, dob

### Products
- `GET /order_flow/api/products` - Get all products (Public)
- `GET /order_flow/api/products/{id}` - Get product by ID (Public)

### Cart Management
- `GET /order_flow/api/cart` - Get user's cart
- `POST /order_flow/api/cart/add` - Add item to cart
- `PUT /order_flow/api/cart/items/{id}` - Update cart item
- `DELETE /order_flow/api/cart/items/{id}` - Remove cart item
- `DELETE /order_flow/api/cart/clear` - Clear cart

### Order Processing
- `GET /order_flow/api/orders` - Get user's orders
- `GET /order_flow/api/orders/{id}` - Get order details
- `POST /order_flow/api/orders` - Create order
- `POST /order_flow/api/orders/{id}/confirm-cod` - Confirm COD order
- `PUT /order_flow/api/orders/{id}/status` - Update order status (Florist only)

### Health Check
- `GET /order_flow/health` - Application health status

## 🏗️ Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+
- Gmail account (for email notifications)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/hoangduc170803/OrderFlow.git
cd OrderFlow
```

2. **Configure Database**
- Create MySQL database: `flower_shop`
- Update `application.yaml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flower_shop
    username: your_username
    password: your_password
```

3. **Configure Email (Optional)**
- Set up Gmail App Password
- Update email configuration in `application.yaml`:
```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password

app:
  notification:
    email:
      from: your-email@gmail.com
      florist: florist@yourdomain.com
```

4. **Run the application**
```bash
mvn spring-boot:run
```

5. **Access the application**
- API Base URL: `http://localhost:8080/order_flow`
- Health Check: `http://localhost:8080/order_flow/health`

## 🗄️ Database Schema

### Core Entities
- **Users** - User accounts with email for notifications
- **Roles** - User roles (CUSTOMER, FLORIST, ADMIN)
- **Permissions** - Role-based permissions
- **Products** - Product catalog with categories
- **Categories** - Product categorization
- **Cart** - Shopping cart management
- **CartItems** - Individual cart items
- **Orders** - Order processing with status tracking
- **OrderItems** - Individual order items
- **InvalidatedToken** - JWT token blacklist

### Key Relationships
- User ↔ Cart (One-to-One)
- User ↔ Order (One-to-Many)
- User ↔ Role (Many-to-Many)
- Product ↔ Category (Many-to-One)
- Order ↔ OrderItem (One-to-Many)

## 🔐 Authentication Flow

1. **Register** → Create user account with email
2. **Login** → Get JWT token
3. **Include token** in Authorization header: `Bearer YOUR_TOKEN`
4. **Access protected endpoints**

### User Registration Example
```json
POST /order_flow/users
{
    "username": "customer1",
    "password": "password123",
    "email": "customer@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "dob": "1990-01-01"
}
```

## 📧 Notification System

### Email Notifications
- **Florist Notification**: Sent when new COD order is confirmed
- **Customer Confirmation**: Sent when order is confirmed
- **Status Updates**: Sent when order status changes (Shipped, Delivered, Cancelled)

### Email Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

app:
  notification:
    enabled: true
    email:
      from: your-email@gmail.com
      florist: florist@yourdomain.com
```

## 💰 COD (Cash on Delivery) Flow

1. **Customer Registration** with email
2. **Browse Products** (public access)
3. **Add to Cart** and manage items
4. **Create Order** with COD payment method
5. **Confirm Order** on website
6. **Email Notifications** sent to florist and customer
7. **Status Updates** by florist with customer notifications

## 🧪 API Testing

### Using Postman Collection
1. Import `COD_Test_Collection.json` into Postman
2. Set environment variables:
   - `token`: JWT token from login
   - `orderId`: Order ID from order creation
3. Run requests in sequence to test complete COD flow

### Manual Testing
```bash
# Register user
curl -X POST http://localhost:8080/order_flow/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123","email":"test@example.com","firstName":"Test","lastName":"User"}'

# Login
curl -X POST http://localhost:8080/order_flow/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'

# View products
curl http://localhost:8080/order_flow/api/products
```

## 📊 Order Status Flow

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↓
CANCELLED
```

- **PENDING**: Order created, waiting for confirmation
- **CONFIRMED**: Customer confirmed COD order, inventory decremented
- **SHIPPED**: Order shipped, customer notified
- **DELIVERED**: Order delivered successfully
- **CANCELLED**: Order cancelled by customer or florist

## 🔧 Configuration

### Application Properties
```yaml
server:
  port: 8080
  servlet:
    context-path: /order_flow

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flower_shop
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password

jwt:
  signerKey: your-secret-key
  valid-duration: 3600 # 1 hour
  refreshable-duration: 36000 # 10 hours

app:
  notification:
    enabled: true
    email:
      from: your-email@gmail.com
      florist: florist@yourdomain.com
```

## 🛡️ Security Features

- **JWT Authentication** with configurable expiration
- **Password Encryption** using BCrypt
- **Role-based Access Control** (RBAC)
- **Token Blacklisting** for secure logout
- **Input Validation** with Bean Validation
- **SQL Injection Protection** with JPA/Hibernate
- **CORS Configuration** for cross-origin requests

## 📈 Performance Features

- **Lazy Loading** for entity relationships
- **Database Indexing** on frequently queried columns
- **Connection Pooling** with HikariCP
- **Query Optimization** with Spring Data JPA
- **Audit Trail** with automatic timestamps
- **Error Handling** with global exception handling

## 🧪 Testing

### Unit Testing
- Entity validation tests
- Service layer tests
- Repository tests
- Controller tests

### Integration Testing
- End-to-end API testing
- Database integration tests
- Email notification tests

### Manual Testing
- Complete COD flow testing
- Email notification verification
- Role-based access testing

## 📝 Error Handling

### Custom Error Codes
- `USER_EXISTED` (1001)
- `PRODUCT_NOT_FOUND` (2001)
- `INSUFFICIENT_STOCK` (2003)
- `CART_NOT_FOUND` (3001)
- `ORDER_NOT_FOUND` (4001)
- `ORDER_ALREADY_CONFIRMED` (4002)

### Global Exception Handler
- Centralized error handling
- Consistent error response format
- Detailed error logging
- User-friendly error messages



### Development Guidelines
- Follow Spring Boot best practices
- Write comprehensive tests
- Update documentation
- Use meaningful commit messages
- Follow Java coding standards

## 👥 Team

- **Backend Development**: Spring Boot, JPA, Security
- **Database Design**: MySQL, Entity Relationships
- **API Design**: RESTful APIs, JWT Authentication
- **Email Integration**: SMTP, HTML Templates
- **Testing**: Unit Tests, Integration Tests

## 📞 Support

For questions, issues, or support:
- **GitHub Issues**: [Create an issue](https://github.com/hoangduc170803/OrderFlow/issues)
- **Documentation**: Check the guides in the repository
- **Email**: hoangduc170803@gmail.com


---

**OrderFlow** - A complete e-commerce solution with COD support and email notifications built with Spring Boot! 🚀
