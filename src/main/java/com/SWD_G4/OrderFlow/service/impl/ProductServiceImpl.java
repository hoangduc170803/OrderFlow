package com.SWD_G4.OrderFlow.service.impl;

import com.SWD_G4.OrderFlow.dto.response.CachedPageData;
import com.SWD_G4.OrderFlow.entity.Product;
import com.SWD_G4.OrderFlow.repository.ProductRepository;
import com.SWD_G4.OrderFlow.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Constructor with optional RedisTemplate and ObjectMapper
    public ProductServiceImpl(ProductRepository productRepository, 
                             @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
                             @Autowired(required = false) @org.springframework.beans.factory.annotation.Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
                             ObjectMapper defaultObjectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        // Use Redis ObjectMapper if available, otherwise use default Spring Boot ObjectMapper
        this.objectMapper = redisObjectMapper != null ? redisObjectMapper : defaultObjectMapper;
    }
    
    @Override
    public Optional<Product> findById(Long productId) {
        String cacheKey = "products::" + productId;
        
        // Try cache if Redis is available
        if (redisTemplate != null) {
            try {
                Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
                
                if (cachedObject != null) {
                    // Convert from LinkedHashMap (deserialized from JSON) to Product
                    Product product;
                    if (cachedObject instanceof Product) {
                        product = (Product) cachedObject;
                    } else {
                        // Deserialize from LinkedHashMap or Map to Product
                        product = objectMapper.convertValue(cachedObject, Product.class);
                    }
                    
                    log.info("📦 Cache HIT - Returning cached product: {}", productId);
                    return Optional.of(product);
                }
            } catch (Exception e) {
                log.warn("Error reading product from cache, falling back to database: {}", e.getMessage());
                log.debug("Cache error details: ", e);
            }
        }
        
        // Cache MISS - Fetch from database
        log.info("🔍 Cache MISS - Fetching product from database: {}", productId);
        Optional<Product> product = productRepository.findById(productId);
        
        // Cache the result if Redis is available and product exists
        if (product.isPresent() && redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, product.get(), 1, TimeUnit.HOURS);
                log.info("✅ Product cached: {} - Next request will use Redis cache", productId);
            } catch (Exception e) {
                log.warn("Error caching product: {}", e.getMessage());
            }
        }
        
        return product;
    }
    
    @Override
    public Page<Product> findActiveProducts(Pageable pageable) {
        String cacheKey = "productList::" + buildPageCacheKey(pageable);
        
        // Try cache if Redis is available
        if (redisTemplate != null) {
            try {
                // Try to get from cache
                Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
                
                if (cachedObject != null) {
                    // Convert from LinkedHashMap (deserialized from JSON) to CachedPageData
                    CachedPageData cached;
                    if (cachedObject instanceof CachedPageData) {
                        cached = (CachedPageData) cachedObject;
                    } else {
                        // Deserialize from LinkedHashMap or Map to CachedPageData
                        cached = objectMapper.convertValue(cachedObject, CachedPageData.class);
                    }
                    
                    log.info(" Cache HIT - Returning cached products page: {}, size: {}, total: {} (cacheKey: {})", 
                            pageable.getPageNumber(), pageable.getPageSize(), cached.getTotalElements(), cacheKey);
                    
                    // Reconstruct Page from cached data
                    return new PageImpl<>(
                        cached.getContent(),
                        pageable,
                        cached.getTotalElements()
                    );
                }
            } catch (Exception e) {
                log.warn("Error reading from cache, falling back to database: {}", e.getMessage());
                log.debug("Cache error details: ", e);
            }
        }
        
        // Cache MISS - Fetch from database
        log.info("🔍 Cache MISS - Fetching active products from database - page: {}, size: {}, sort: {} (cacheKey: {})", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), cacheKey);
        
        Page<Product> products = productRepository.findByIsActiveTrue(pageable);
        
        // Cache the result if Redis is available
        if (redisTemplate != null) {
            try {
                CachedPageData cacheData = CachedPageData.builder()
                        .content(products.getContent())
                        .totalElements(products.getTotalElements())
                        .totalPages(products.getTotalPages())
                        .pageNumber(products.getNumber())
                        .pageSize(products.getSize())
                        .first(products.isFirst())
                        .last(products.isLast())
                        .build();
                
                // Store in Redis with TTL of 1 hour
                redisTemplate.opsForValue().set(cacheKey, cacheData, 1, TimeUnit.HOURS);
                
                log.info(" Products page cached - page: {}, size: {}, total: {} (cacheKey: {}) - Next request will use Redis cache", 
                        pageable.getPageNumber(), pageable.getPageSize(), products.getTotalElements(), cacheKey);
            } catch (Exception e) {
                log.warn("Error caching products page: {}", e.getMessage());
            }
        }
        
        return products;
    }
    
    /**
     * Build a stable cache key from Pageable for logging purposes
     */
    private String buildPageCacheKey(Pageable pageable) {
        StringBuilder key = new StringBuilder("active_page_");
        key.append(pageable.getPageNumber());
        key.append("_size_").append(pageable.getPageSize());
        
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            key.append("_sort_");
            pageable.getSort().forEach(order -> {
                key.append(order.getProperty()).append("_").append(order.getDirection().name()).append("_");
            });
            // Remove trailing underscore
            if (key.length() > 0 && key.charAt(key.length() - 1) == '_') {
                key.setLength(key.length() - 1);
            }
        } else {
            key.append("_sort_none");
        }
        
        return key.toString();
    }
    
    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        String cacheKey = "productList::category_" + categoryId;
        
        // Try cache if Redis is available
        if (redisTemplate != null) {
            try {
                Object cachedObject = redisTemplate.opsForValue().get(cacheKey);
                
                if (cachedObject != null) {
                    // Convert from List of LinkedHashMaps to List of Products
                    List<Product> products;
                    if (cachedObject instanceof List<?>) {
                        List<?> list = (List<?>) cachedObject;
                        if (!list.isEmpty() && list.get(0) instanceof Product) {
                            products = (List<Product>) cachedObject;
                        } else {
                            // Deserialize from List of LinkedHashMaps to List of Products
                            products = objectMapper.convertValue(
                                cachedObject, 
                                objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class)
                            );
                        }
                        
                        log.info("Cache HIT - Returning cached products for category: {} ({} products)", categoryId, products.size());
                        return products;
                    }
                }
            } catch (Exception e) {
                log.warn("Error reading products by category from cache, falling back to database: {}", e.getMessage());
                log.debug("Cache error details: ", e);
            }
        }
        
        // Cache MISS - Fetch from database
        log.info("🔍 Cache MISS - Fetching products by category from database: {}", categoryId);
        List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
        
        // Cache the result if Redis is available and list is not empty
        if (!products.isEmpty() && redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, products, 1, TimeUnit.HOURS);
                log.info(" Products cached for category: {} ({} products) - Next request will use Redis cache", categoryId, products.size());
            } catch (Exception e) {
                log.warn("Error caching products by category: {}", e.getMessage());
            }
        }
        
        return products;
    }
    
    @Override
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public Product save(Product product) {
        log.info("💾 Saving product: {} - Invalidating ALL cache (products + productList)", product.getId() != null ? product.getId() : "new");
        Product savedProduct = productRepository.save(product);
        
        // Clear pagination cache manually
        clearPaginationCache();
        
        log.info(" Cache invalidated - All product caches cleared (hotswap)");
        return savedProduct;
    }
    
    @Override
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public void deleteById(Long productId) {
        log.info(" Deleting product: {} - Invalidating ALL cache", productId);
        productRepository.deleteById(productId);
        
        // Clear pagination cache manually
        clearPaginationCache();
        
        log.info("✅ Cache invalidated - All product caches cleared");
    }
    
    @Override
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public void invalidateCache(Long productId) {
        log.info(" Invalidating cache for product: {} - Clearing all product caches", productId);
        // Cache eviction handled by @CacheEvict annotation
        clearPaginationCache();
    }
    
    @Override
    @CacheEvict(value = {"products", "productList"}, allEntries = true)
    public void invalidateAllCache() {
        log.info(" Invalidating ALL product caches");
        // Cache eviction handled by @CacheEvict annotation
        clearPaginationCache();
    }
    
    /**
     * Clear all pagination cache entries
     */
    private void clearPaginationCache() {
        if (redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys("productList::active_page_*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    log.info(" Cleared {} pagination cache entries", keys.size());
                }
            } catch (Exception e) {
                log.warn("Error clearing pagination cache: {}", e.getMessage());
            }
        }
    }
}

