package com.SWD_G4.OrderFlow.service;

import com.SWD_G4.OrderFlow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    /**
     * Get product by ID with cache support
     * @param productId the product ID
     * @return Optional containing the product if found
     */
    Optional<Product> findById(Long productId);
    
    /**
     * Get all active products with pagination and cache support
     * @param pageable pagination parameters
     * @return Page of products
     */
    Page<Product> findActiveProducts(Pageable pageable);
    
    /**
     * Get products by category ID with cache support
     * @param categoryId the category ID
     * @return List of products
     */
    List<Product> findByCategoryId(Long categoryId);
    
    /**
     * Save or update product (invalidates cache)
     * @param product the product to save
     * @return the saved product
     */
    Product save(Product product);
    
    /**
     * Delete product by ID (invalidates cache)
     * @param productId the product ID
     */
    void deleteById(Long productId);
    
    /**
     * Invalidate product cache (hotswap support)
     * @param productId the product ID to invalidate
     */
    void invalidateCache(Long productId);
    
    /**
     * Invalidate all product caches
     */
    void invalidateAllCache();
}

