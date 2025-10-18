# Hướng dẫn tạo Entity từ Database

## 1. Các bước tạo Entity từ bảng Database

### Bước 1: Phân tích cấu trúc bảng
- Xác định các cột trong bảng
- Xác định kiểu dữ liệu của từng cột
- Xác định các ràng buộc (constraints): PRIMARY KEY, FOREIGN KEY, NOT NULL, UNIQUE
- Xác định mối quan hệ giữa các bảng

### Bước 2: Tạo Entity Class
```java
@Entity
@Table(name = "table_name")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EntityName {
    // Các trường dữ liệu
}
```

### Bước 3: Mapping các cột
```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id;

@Column(name = "column_name", nullable = false, unique = true)
private String fieldName;
```

## 2. Các Annotation JPA quan trọng

### @Entity
- Đánh dấu class là một entity JPA
- Phải có constructor không tham số

### @Table
- Chỉ định tên bảng trong database
- Có thể chỉ định schema, catalog

### @Id
- Đánh dấu trường là Primary Key

### @GeneratedValue
- Tự động tạo giá trị cho Primary Key
- Các strategy: AUTO, IDENTITY, SEQUENCE, TABLE, UUID

### @Column
- Mapping trường với cột trong database
- Các thuộc tính: name, nullable, unique, length, precision, scale

### @Enumerated
- Mapping enum với database
- EnumType.STRING: lưu tên enum
- EnumType.ORDINAL: lưu thứ tự enum

## 3. Mối quan hệ giữa các Entity

### @OneToOne
```java
@OneToOne(mappedBy = "entity", cascade = CascadeType.ALL)
private RelatedEntity relatedEntity;
```

### @OneToMany
```java
@OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<RelatedEntity> relatedEntities;
```

### @ManyToOne
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "foreign_key_column")
private RelatedEntity relatedEntity;
```

### @ManyToMany
```java
@ManyToMany
@JoinTable(
    name = "junction_table",
    joinColumns = @JoinColumn(name = "entity_id"),
    inverseJoinColumns = @JoinColumn(name = "related_entity_id")
)
private Set<RelatedEntity> relatedEntities;
```

## 4. JPA Auditing

### Bật JPA Auditing
```java
@SpringBootApplication
@EnableJpaAuditing
public class Application {
    // ...
}
```

### Sử dụng Auditing
```java
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

## 5. Fetch Types

### EAGER vs LAZY
- **EAGER**: Load ngay lập tức khi load entity
- **LAZY**: Load khi cần thiết (mặc định cho @OneToMany, @ManyToMany)

### Khuyến nghị
- Sử dụng LAZY cho hầu hết các mối quan hệ
- Sử dụng JOIN FETCH trong query khi cần load liên quan

## 6. Cascade Types

### Các loại Cascade
- **ALL**: Tất cả operations
- **PERSIST**: Chỉ persist
- **MERGE**: Chỉ merge
- **REMOVE**: Chỉ remove
- **REFRESH**: Chỉ refresh
- **DETACH**: Chỉ detach

## 7. Ví dụ thực tế

### Entity User
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

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

## 8. Best Practices

1. **Đặt tên**: Sử dụng tên rõ ràng, có ý nghĩa
2. **Primary Key**: Sử dụng UUID cho distributed systems
3. **Timestamps**: Luôn có created_at và updated_at
4. **Validation**: Sử dụng Bean Validation annotations
5. **Lazy Loading**: Sử dụng LAZY cho performance
6. **Cascade**: Cẩn thận với cascade operations
7. **Indexes**: Tạo indexes cho các cột thường query

## 9. Tools hỗ trợ

### IDE Plugins
- JPA Buddy (IntelliJ IDEA)
- Spring Tools (Eclipse/VS Code)

### Database Tools
- DBeaver
- MySQL Workbench
- pgAdmin (PostgreSQL)

### Code Generation
- JPA Buddy có thể generate entity từ database
- Spring Boot có thể reverse engineer từ database
