package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final RoleRepository roleRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connection
            long roleCount = roleRepository.count();
            health.put("database", "connected");
            health.put("roleCount", roleCount);
            health.put("status", "healthy");
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .code(1000)
                    .message("Health check successful")
                    .result(health)
                    .build());
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            health.put("database", "disconnected");
            health.put("error", e.getMessage());
            health.put("status", "unhealthy");
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .code(9999)
                    .message("Health check failed")
                    .result(health)
                    .build());
        }
    }
}
