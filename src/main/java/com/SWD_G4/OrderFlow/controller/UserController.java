package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.request.UserCreationRequest;
import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.dto.response.RoleResponse;
import com.SWD_G4.OrderFlow.dto.response.UserResponse;
import com.SWD_G4.OrderFlow.entity.Role;
import com.SWD_G4.OrderFlow.entity.User;
import com.SWD_G4.OrderFlow.exception.AppException;
import com.SWD_G4.OrderFlow.exception.ErrorCode;
import com.SWD_G4.OrderFlow.repository.RoleRepository;
import com.SWD_G4.OrderFlow.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreationRequest request) {
        try {
            log.info("Creating user with username: {}", request.getUsername());
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        
        // Get default role (CUSTOMER)
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseGet(() -> {
                    // Create CUSTOMER role if it doesn't exist
                    Role newRole = Role.builder()
                            .name("CUSTOMER")
                            .description("Customer role for regular users")
                            .build();
                    return roleRepository.save(newRole);
                });
        
        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .roles(Set.of(customerRole))
                .build();
        
        user = userRepository.save(user);
        
        // Load user with roles
        user = userRepository.findByIdWithRoles(user.getId()).orElse(user);
        
        log.info("User created with roles: {}", user.getRoles());
        
        // Convert roles to RoleResponse
        java.util.Set<RoleResponse> roleResponses = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleResponses = new java.util.HashSet<>();
            for (com.SWD_G4.OrderFlow.entity.Role role : user.getRoles()) {
                RoleResponse roleResponse = RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build();
                roleResponses.add(roleResponse);
            }
        }
        
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDob())
                .roles(roleResponses)
                .createdAt(user.getCreatedAt())
                .build();
        
            return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                    .code(1000)
                    .message("User created successfully")
                    .result(userResponse)
                    .build());
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw e;
        }
    }
}
