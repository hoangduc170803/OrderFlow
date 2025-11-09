# OrderFlow - Spring Boot E-commerce Application

A comprehensive e-commerce application built with Spring Boot, featuring JWT authentication, cart management, product catalog, order processing, Redis caching, Kafka messaging, and real-time notifications with Cash on Delivery (COD) support.

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
- 🚀 **Redis Cache** for Product Service with hotswap support
- 📨 **Kafka Messaging** for real-time notifications
- 👥 **Multi-Florist Notifications** - Send notifications to all users with FLORIST role
  
## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.6
- **Security**: Spring Security with JWT
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA with Hibernate
- **Cache**: Redis 7+ (for product caching)
- **Messaging**: Apache Kafka 7.5+ (for real-time notifications)
- **Email**: Spring Boot Mail with Gmail SMTP
- **Build Tool**: Maven
- **Java Version**: 17+
- **Validation**: Bean Validation (JSR-303)
- **Logging**: SLF4J with Logback
- **Containerization**: Docker & Docker Compose

## 📚 Documentation

- **[Entity Guide](ENTITY_GUIDE.md)** - Complete entity structure and relationships
- **[COD Flow Guide](COD_FLOW_GUIDE.md)** - Cash on Delivery implementation
- **[Notification System Guide](NOTIFICATION_SYSTEM_GUIDE.md)** - Email and Kafka notification system
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
- `GET /order_flow/api/products` - Get all products with pagination (Public, Cached)
  - Query params: `page`, `size`, `sortBy`, `sortDir`
  - Example: `/api/products?page=0&size=10&sortBy=name&sortDir=asc`
- `GET /order_flow/api/products/{id}` - Get product by ID (Public, Cached)
- `GET /order_flow/api/products/category/{categoryId}` - Get products by category (Public, Cached)

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
- Docker & Docker Compose (for Redis and Kafka)
- Gmail account (for email notifications)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/hoangduc170803/OrderFlow.git
cd OrderFlow
```

2. **Start Redis and Kafka with Docker Compose**
```bash
# Start Redis, Kafka, Zookeeper, and Kafka UI
docker-compose up -d

# Verify services are running
docker-compose ps

# Check Redis
docker-compose exec redis redis-cli ping
# Should return: PONG

# Check Kafka topics (after first run)
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

**Services:**
- **Redis**: `localhost:6379` (Product caching)
- **Kafka**: `localhost:9092` (Real-time notifications)
- **Kafka UI**: `http://localhost:8081` (Kafka management interface)
- **Zookeeper**: `localhost:2181` (Required for Kafka)

3. **Configure Database**
- Create MySQL database: `flower_shop`
- Update `application.yaml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/flower_shop
    username: your_username
    password: your_password
```

4. **Configure Email**
- Set up Gmail App Password
- Update email configuration in `application.yaml`:
```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-app-password

app:
  notification:
    enabled: true
    kafka:
      enabled: true  # Enable Kafka for real-time notifications
    email:
      enabled: true  # Enable email notifications
      from: your-email@gmail.com
      # florist: optional-fallback@email.com  # Only used if no FLORIST users found
```

5. **Configure Redis Cache (Optional)**
- Redis is automatically configured when running via Docker Compose
- Cache TTL: 1 hour (configurable in `application.yaml`)
- Fallback: In-memory cache if Redis is unavailable

6. **Run the application**
```bash
mvn spring-boot:run
```

7. **Access the application**
- API Base URL: `http://localhost:8080/order_flow`
- Health Check: `http://localhost:8080/order_flow/health`
- Kafka UI: `http://localhost:8081`

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

### Notification Features
- **Kafka Integration**: Real-time notifications via Kafka topics
- **Email Notifications**: HTML email templates for all notifications
- **Multi-Florist Support**: Automatically sends notifications to all users with FLORIST role
- **Dual Delivery**: Both Kafka and Email notifications sent concurrently
- **Fallback Support**: Email fallback if Kafka fails (configurable)

### Notification Types

1. **Florist Notification**
   - Triggered when: New COD order is confirmed
   - Recipients: All users with FLORIST role (with email)
   - Channels: Kafka topic `florist-notifications` + Email
   - Content: Order details, customer info, order items

2. **Customer Confirmation**
   - Triggered when: Order is confirmed
   - Recipients: Order customer
   - Channels: Kafka topic `order-notifications` + Email
   - Content: Order confirmation, payment info (COD)

3. **Status Updates**
   - Triggered when: Order status changes (SHIPPED, DELIVERED, CANCELLED)
   - Recipients: Order customer
   - Channels: Kafka topic `order-status-updates` + Email
   - Content: Status-specific messages

### Kafka Topics
- `florist-notifications`: Notifications for florists about new orders
- `order-notifications`: Customer order confirmations
- `order-status-updates`: Order status change notifications

### Multi-Florist Notification
The system automatically:
1. Queries all users with `FLORIST` role from database
2. Sends notification (Kafka + Email) to each florist's email
3. Falls back to configured email if no florists found (optional)

### Configuration
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
  kafka:
    bootstrap-servers: localhost:9092

app:
  notification:
    enabled: true
    kafka:
      enabled: true  # Enable Kafka for real-time notifications
    email:
      enabled: true  # Enable email notifications (sends independently)
      from: your-email@gmail.com
      # florist: optional-fallback@email.com  # Only used if no FLORIST users found
      fallback: true  # Send email as fallback if Kafka fails
```

### Setting Up Florist Users
To send notifications to florists, create users with FLORIST role:
```sql
-- Create FLORIST role if not exists
INSERT INTO roles (name, description) VALUES ('FLORIST', 'Florist role') 
ON DUPLICATE KEY UPDATE name=name;

-- Assign FLORIST role to user (replace user_id and role_id)
INSERT INTO user_role (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'florist_username' AND r.name = 'FLORIST';
```

## 💰 COD (Cash on Delivery) Flow

1. **Customer Registration** with email
2. **Browse Products** (public access, cached in Redis)
3. **Add to Cart** and manage items
4. **Create Order** with COD payment method
5. **Confirm Order** on website
6. **Notifications Sent**:
   - **Kafka**: Real-time notifications to Kafka topics
   - **Email**: HTML emails to all florists (with FLORIST role) and customer
7. **Status Updates** by florist with customer notifications
8. **Cache Invalidation**: Product cache automatically cleared on updates (hotswap)

## 🧪 API Testing

### Using Postman Collection
1. Import `COD_Test_Collection.json` into Postman
2. Set environment variables:
   - `token`: JWT token from login
   - `orderId`: Order ID from order creation
3. Run requests in sequence to test complete COD flow
4. Check Kafka UI at `http://localhost:8081` to verify Kafka messages
5. Verify Redis cache with: `docker-compose exec redis redis-cli KEYS "*"`

### Manual Testing

#### Test Product Cache
```bash
# First request - Cache MISS (will query database)
curl http://localhost:8080/order_flow/api/products?page=0&size=10

# Second request - Cache HIT (will use Redis cache, no database query)
curl http://localhost:8080/order_flow/api/products?page=0&size=10

# Check Redis cache
docker-compose exec redis redis-cli KEYS "productList::*"
```

#### Test Notifications
```bash
# Register user
curl -X POST http://localhost:8080/order_flow/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123","email":"test@example.com","firstName":"Test","lastName":"User"}'

# Login
curl -X POST http://localhost:8080/order_flow/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'

# View products (cached)
curl http://localhost:8080/order_flow/api/products

# Create and confirm COD order
# Check Kafka UI: http://localhost:8081
# Check email inbox for notifications
```

### Testing Redis Cache
```bash
# Check Redis connection
docker-compose exec redis redis-cli ping

# View all cache keys
docker-compose exec redis redis-cli KEYS "*"

# View specific cache entry
docker-compose exec redis redis-cli GET "productList::active_page_0_size_10_sort_name_ASC"

# Clear all cache
docker-compose exec redis redis-cli FLUSHALL
```

### Testing Kafka
```bash
# List Kafka topics
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# View messages in a topic
docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic florist-notifications --from-beginning

# Access Kafka UI
# Open browser: http://localhost:8081
# View topics and messages
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
  
  # Redis Configuration
  redis:
    host: localhost
    port: 6379
    password: # Leave empty if no password
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
  data:
    redis:
      repositories:
        enabled: false  # Disable Redis repositories - we only use Redis for caching
  cache:
    type: redis  # Using Redis cache
    redis:
      time-to-live: 3600000 # 1 hour in milliseconds
      cache-null-values: false
    cache-names: products,productList
  
  # Kafka Configuration
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      enable-idempotence: true
    consumer:
      group-id: orderflow-notification-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      properties:
        spring.json.trusted.packages: "*"
  
  # Email Configuration
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
    kafka:
      enabled: true  # Enable Kafka for real-time notifications
    email:
      enabled: true  # Enable email notifications (sends independently)
      from: your-email@gmail.com
      # florist: optional-fallback@email.com  # Only used if no FLORIST users found
      fallback: true  # Send email as fallback if Kafka fails
```

### Docker Compose Services
```yaml
# Redis: localhost:6379
# Kafka: localhost:9092
# Kafka UI: http://localhost:8081
# Zookeeper: localhost:2181
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

### Redis Cache
- **Product Caching**: Products cached in Redis with 1-hour TTL
- **Pagination Cache**: Cached paginated product lists with smart key generation
- **Hotswap Support**: Automatic cache invalidation on product updates
- **Fallback**: In-memory cache if Redis is unavailable

### Cache Strategy
- **Single Product**: Cached by product ID (`products::{productId}`)
- **Product List by Category**: Cached by category ID (`productList::category_{categoryId}`)
- **Paginated Products**: Cached by page, size, and sort (`productList::active_page_{page}_size_{size}_sort_{sort}`)
- **Cache Invalidation**: Automatic on product save/update/delete (hotswap)
- **TTL**: 1 hour (configurable)

### Performance Optimizations
- **Lazy Loading** for entity relationships
- **Database Indexing** on frequently queried columns
- **Connection Pooling** with HikariCP
- **Query Optimization** with Spring Data JPA
- **Redis Caching** for frequently accessed data
- **Audit Trail** with automatic timestamps
- **Error Handling** with global exception handling

## 🔍 Monitoring & Debugging

### Redis Monitoring
```bash
# Check Redis status
docker-compose exec redis redis-cli ping

# Monitor Redis commands
docker-compose exec redis redis-cli MONITOR

# Check cache statistics
docker-compose exec redis redis-cli INFO stats

# View all cache keys
docker-compose exec redis redis-cli KEYS "*"

# View specific cache entry
docker-compose exec redis redis-cli GET "productList::active_page_0_size_10_sort_name_ASC"
```

### Kafka Monitoring
- **Kafka UI**: `http://localhost:8081`
  - View topics and partitions
  - Monitor message flow
  - Check consumer groups
  - View message contents
- **Command Line**:
  ```bash
  # List topics
  docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
  
  # View messages
  docker-compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 \
    --topic florist-notifications --from-beginning
  ```

### Application Logs
- Check application logs for cache hits/misses
- Monitor Kafka producer/consumer logs
- Verify email sending status
- Track notification delivery

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
- **Cache Integration**: Redis for product caching with hotswap
- **Messaging**: Apache Kafka for real-time notifications
- **Email Integration**: SMTP, HTML Templates
- **Testing**: Unit Tests, Integration Tests

## 📞 Support

For questions, issues, or support:
- **GitHub Issues**: [Create an issue](https://github.com/hoangduc170803/OrderFlow/issues)
- **Documentation**: Check the guides in the repository
- **Email**: hoangduc170803@gmail.com


---

**OrderFlow** - A complete e-commerce solution with COD support, Redis caching, Kafka messaging, and real-time notifications built with Spring Boot! 🚀
