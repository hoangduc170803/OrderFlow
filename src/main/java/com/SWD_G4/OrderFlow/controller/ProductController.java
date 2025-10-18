package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.entity.Product;
import com.SWD_G4.OrderFlow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    private final ProductRepository productRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        
        return ResponseEntity.ok(ApiResponse.<Page<Product>>builder()
                .code(1000)
                .message("Get products successfully")
                .result(products)
                .build());
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
                .orElse(null);
        
        if (product == null || !product.getIsActive()) {
            return ResponseEntity.ok(ApiResponse.<Product>builder()
                    .code(2001)
                    .message("Product not found")
                    .result(null)
                    .build());
        }
        
        return ResponseEntity.ok(ApiResponse.<Product>builder()
                .code(1000)
                .message("Get product successfully")
                .result(product)
                .build());
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        
        return ResponseEntity.ok(ApiResponse.<List<Product>>builder()
                .code(1000)
                .message("Get products by category successfully")
                .result(products)
                .build());
    }
}
