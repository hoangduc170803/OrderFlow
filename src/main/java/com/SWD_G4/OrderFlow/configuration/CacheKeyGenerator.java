package com.SWD_G4.OrderFlow.configuration;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

@Component("pageableKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {
    
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length > 0 && params[0] instanceof Pageable pageable) {
            return buildPageableCacheKey(pageable);
        }
        return method.getName() + "_" + java.util.Arrays.toString(params);
    }
    
    private String buildPageableCacheKey(Pageable pageable) {
        StringBuilder key = new StringBuilder("active_page_");
        key.append(pageable.getPageNumber());
        key.append("_size_").append(pageable.getPageSize());
        
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            key.append("_sort_");
            String sortKey = pageable.getSort().stream()
                    .map(order -> order.getProperty() + "_" + order.getDirection().name())
                    .collect(Collectors.joining("_"));
            key.append(sortKey);
        } else {
            key.append("_sort_none");
        }
        
        return key.toString();
    }
}

