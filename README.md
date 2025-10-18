# OrderFlow - Spring Boot E-commerce Application

A comprehensive e-commerce application built with Spring Boot, featuring JWT authentication, cart management, product catalog, and order processing.

## Features

- 🔐 **JWT Authentication & Authorization**
- 🛒 **Shopping Cart Management**
- 📦 **Product Catalog**
- 👥 **User Management**
- 📋 **Order Processing**
- 🏷️ **Category Management**
- 🔒 **Role-based Access Control**

## Tech Stack

- **Backend**: Spring Boot 3.x
- **Security**: Spring Security with JWT
- **Database**: MySQL
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Java Version**: 17+

## API Endpoints

### Authentication
- `POST /order_flow/auth/token` - Login
- `POST /order_flow/auth/refresh` - Refresh token
- `POST /order_flow/auth/logout` - Logout
- `POST /order_flow/auth/introspect` - Token introspection

### Products
- `GET /order_flow/api/products` - Get all products (Public)
- `GET /order_flow/api/products/{id}` - Get product by ID (Public)

### Cart
- `GET /order_flow/api/cart` - Get user's cart
- `POST /order_flow/api/cart/add` - Add item to cart
- `PUT /order_flow/api/cart/items/{id}` - Update cart item
- `DELETE /order_flow/api/cart/items/{id}` - Remove cart item
- `DELETE /order_flow/api/cart/clear` - Clear cart

### Users
- `POST /order_flow/users` - Create user account

### Orders
- `GET /order_flow/api/orders` - Get user's orders
- `POST /order_flow/api/orders` - Create order

## Getting Started

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/orderflow-spring-boot.git
cd orderflow-spring-boot
```

2. **Configure Database**
- Create MySQL database: `flower_shop`
- Update `application.yaml` with your database credentials

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
- API Base URL: `http://localhost:8080/order_flow`

## Database Schema

The application uses the following main entities:
- **Users** - User accounts and authentication
- **Products** - Product catalog
- **Categories** - Product categorization
- **Cart** - Shopping cart
- **CartItems** - Individual cart items
- **Orders** - Order processing
- **OrderItems** - Individual order items
- **Roles & Permissions** - Authorization system

## Authentication Flow

1. **Register/Login** → Get JWT token
2. **Include token** in Authorization header: `Bearer YOUR_TOKEN`
3. **Access protected endpoints**

## Configuration

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

jwt:
  signerKey: your-secret-key
  valid-duration: 3600 # 1 hour
  refreshable-duration: 36000 # 10 hours
```

## API Testing

Use the provided Postman collection: `COD_Test_Collection.json`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please open an issue on GitHub.
