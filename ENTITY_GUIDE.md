# Entity Guide - OrderFlow Application

## Overview
This document provides a comprehensive guide to the entity structure and relationships in the OrderFlow application. The application uses Spring Data JPA with MySQL database to manage e-commerce functionality including users, products, orders, and carts with email notification support.

## Entity Architecture

### Core Entities
The OrderFlow application consists of 11 main entities organized into logical groups:

1. **Authentication & Authorization**
   - `User` - User accounts and authentication (with email support)
   - `Role` - User roles and permissions
   - `Permission` - Individual permissions
   - `InvalidatedToken` - JWT token management

2. **Product Management**
   - `Product` - Product catalog
   - `Category` - Product categorization

3. **Shopping & Orders**
   - `Cart` - Shopping cart
   - `CartItem` - Individual cart items
   - `Order` - Order processing
   - `OrderItem` - Individual order items

4. **Enums**
   - `PaymentMethod` - Payment method enumeration

## Entity Details

### 1. User Entity
```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "dob")
    private LocalDate dob;

    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- **Primary Key**: Auto-increment Long ID
- **Authentication**: Username and password for login
- **Profile Information**: Email, first name, last name, date of birth
- **Email Support**: Email field for notifications (added in recent updates)
- **Role-based Access**: Many-to-many relationship with Role entity
- **Auditing**: Automatic creation and update timestamps

### 2. Role Entity
```java
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- **Role-based Security**: Defines user roles (e.g., ADMIN, CUSTOMER, FLORIST)
- **Permission Management**: Many-to-many relationship with Permission entity
- **Descriptive**: Name and description for role clarity

### 3. Product Entity
```java
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- **Product Information**: Name, description, price, stock quantity
- **Media Support**: Image URL for product photos
- **Category Association**: Many-to-one relationship with Category
- **Active Status**: Soft delete capability with isActive flag
- **Precision Pricing**: BigDecimal with 10 digits, 2 decimal places

### 4. Category Entity
```java
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- **Product Organization**: Groups products into categories
- **Unique Names**: Each category has a unique name
- **Bidirectional Relationship**: One-to-many with Product entity

### 5. Cart Entity
```java
@Entity
@Table(name = "carts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<CartItem> cartItems;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void calculateTotalAmount() {
        if (cartItems != null && !cartItems.isEmpty()) {
            this.totalAmount = cartItems.stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }
}
```

**Key Features:**
- **One Cart Per User**: One-to-one relationship with User
- **Dynamic Total**: Automatic calculation of total amount
- **Eager Loading**: Cart items loaded immediately for performance
- **Orphan Removal**: Automatic cleanup of removed cart items

### 6. CartItem Entity
```java
@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
```

**Key Features:**
- **Price Snapshot**: Stores unit price at time of adding to cart
- **Automatic Calculation**: PrePersist/PreUpdate hooks for total price
- **Product Reference**: Links to Product entity with eager loading

### 7. Order Entity
```java
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
```

**Key Features:**
- **Unique Order Number**: Human-readable order identifier
- **Status Tracking**: Enum-based order status management
- **Payment Method**: Enum-based payment method selection
- **Shipping Information**: Address and notes for delivery
- **Order Items**: One-to-many relationship with OrderItem

### 8. OrderItem Entity
```java
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

**Key Features:**
- **Historical Data**: Preserves product information at time of order
- **Price Snapshot**: Unit price captured when order is placed
- **Quantity Tracking**: Number of items ordered

### 9. PaymentMethod Enum
```java
public enum PaymentMethod {
    COD, // Cash on Delivery
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET
}
```

**Key Features:**
- **Payment Options**: Multiple payment methods supported
- **COD Support**: Cash on Delivery for local orders

## Entity Relationships

### Relationship Diagram
```
User (1) ←→ (1) Cart
User (1) ←→ (*) Order
User (*) ←→ (*) Role
Role (*) ←→ (*) Permission

Product (1) ←→ (*) CartItem
Product (1) ←→ (*) OrderItem
Product (*) ←→ (1) Category

Cart (1) ←→ (*) CartItem
Order (1) ←→ (*) OrderItem

Category (1) ←→ (*) Product
```

### Key Relationships

1. **User ↔ Cart**: One-to-One
   - Each user has exactly one cart
   - Cart belongs to one user

2. **User ↔ Order**: One-to-Many
   - User can have multiple orders
   - Order belongs to one user

3. **User ↔ Role**: Many-to-Many
   - User can have multiple roles
   - Role can be assigned to multiple users

4. **Role ↔ Permission**: Many-to-Many
   - Role can have multiple permissions
   - Permission can be assigned to multiple roles

5. **Product ↔ Category**: Many-to-One
   - Product belongs to one category
   - Category can have multiple products

6. **Cart ↔ CartItem**: One-to-Many
   - Cart can have multiple items
   - CartItem belongs to one cart

7. **Order ↔ OrderItem**: One-to-Many
   - Order can have multiple items
   - OrderItem belongs to one order

## JPA Annotations and Best Practices

### Core Annotations Used

1. **@Entity**: Marks class as JPA entity
2. **@Table**: Maps entity to database table
3. **@Id**: Marks primary key field
4. **@GeneratedValue**: Auto-generates primary key values
5. **@Column**: Maps field to database column
6. **@Enumerated**: Maps enum to database
7. **@ManyToOne, @OneToMany, @OneToOne, @ManyToMany**: Relationship mappings
8. **@JoinColumn**: Specifies foreign key column
9. **@JoinTable**: Specifies junction table for many-to-many
10. **@CreatedDate, @LastModifiedDate**: Automatic timestamp management

### Lombok Annotations

1. **@Data**: Generates getters, setters, toString, equals, hashCode
2. **@Builder**: Builder pattern implementation
3. **@NoArgsConstructor**: No-argument constructor
4. **@AllArgsConstructor**: All-argument constructor

### Best Practices Implemented

1. **Auditing**: All entities have creation and update timestamps
2. **Lazy Loading**: Used for most relationships to improve performance
3. **Eager Loading**: Used selectively where immediate access is needed
4. **Precision for Money**: BigDecimal with appropriate precision and scale
5. **Cascade Operations**: Properly configured for parent-child relationships
6. **Orphan Removal**: Automatic cleanup of dependent entities
7. **JSON Ignore**: Prevents infinite recursion in JSON serialization

## Database Schema

### Table Structure
```sql
-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    dob DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
);

-- Roles table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- User-Role junction table
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_url VARCHAR(500),
    category_id BIGINT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_category (category_id),
    INDEX idx_active (is_active)
);

-- Carts table
CREATE TABLE carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Cart Items table
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES carts(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_cart (cart_id)
);

-- Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL,
    shipping_address VARCHAR(500),
    notes VARCHAR(500),
    payment_method ENUM('COD', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'DIGITAL_WALLET') NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_order_number (order_number)
);

-- Order Items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order (order_id)
);
```

## Performance Considerations

### Indexing Strategy
1. **Primary Keys**: Auto-increment BIGINT for performance
2. **Foreign Keys**: Indexed for join performance
3. **Unique Constraints**: Username, email, order_number indexed
4. **Search Fields**: Category, status, active flags indexed

### Fetch Strategies
1. **LAZY Loading**: Default for most relationships
2. **EAGER Loading**: Used for Cart-CartItem and Product in CartItem
3. **Batch Loading**: Consider for large collections

### Query Optimization
1. **N+1 Problem Prevention**: Use JOIN FETCH in repositories
2. **Pagination**: Implement for large result sets
3. **Projection**: Use DTOs for specific field selection

## Validation and Constraints

### Database Constraints
1. **NOT NULL**: Required fields marked as non-nullable
2. **UNIQUE**: Username, email, order_number, category name
3. **Foreign Keys**: Referential integrity maintained
4. **Check Constraints**: Enum values enforced

### Application-Level Validation
1. **Bean Validation**: Use @Valid annotations
2. **Custom Validators**: Date of birth validation
3. **Business Rules**: Stock quantity checks, price validation

## Migration and Evolution

### Schema Evolution
1. **Backward Compatibility**: New fields nullable by default
2. **Data Migration**: Scripts for existing data
3. **Version Control**: Database migration scripts

### Recent Updates
1. **Email Field**: Added to User entity for notification support
2. **Notification Integration**: User entity supports email notifications

### Future Enhancements
1. **Soft Delete**: Implement for all entities
2. **Versioning**: Optimistic locking for concurrent updates
3. **Audit Trail**: Track all changes with user information
4. **Multi-tenancy**: Support for multiple organizations

## Testing Strategy

### Unit Testing
1. **Entity Tests**: Test entity behavior and relationships
2. **Validation Tests**: Test constraints and business rules
3. **Builder Tests**: Test Lombok builder functionality

### Integration Testing
1. **Repository Tests**: Test database operations
2. **Relationship Tests**: Test entity relationships
3. **Transaction Tests**: Test rollback scenarios

## Conclusion

The OrderFlow entity structure provides a robust foundation for an e-commerce application with:

### ✅ **Key Strengths**
- **Comprehensive Coverage**: All major e-commerce entities included
- **Proper Relationships**: Well-defined entity relationships
- **Performance Optimized**: Appropriate fetch strategies and indexing
- **Audit Trail**: Complete audit information for all entities
- **Flexible Design**: Extensible for future requirements
- **Email Support**: User entity supports notification system

### 🎯 **Business Value**
- **Scalable Architecture**: Supports growth and feature additions
- **Data Integrity**: Strong constraints and relationships
- **Performance**: Optimized for common query patterns
- **Maintainability**: Clean, well-documented entity structure
- **Notification Ready**: Email field supports notification system

This entity design successfully supports the OrderFlow application's requirements for user management, product catalog, shopping cart functionality, order processing, and email notifications with proper separation of concerns and maintainable code structure.