package com.SWD_G4.OrderFlow.repository;

import com.SWD_G4.OrderFlow.entity.Product;
import com.SWD_G4.OrderFlow.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(Category category);
    
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    List<Product> findByIsActiveTrue();
    
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchByNameOrDescription(@Param("keyword") String keyword);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true")
    List<Product> findAvailableProducts();
    
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
}
