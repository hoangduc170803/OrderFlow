package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.request.UserCreationRequest;
import com.SWD_G4.OrderFlow.dto.request.UserUpdateRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dob(request.getDob())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .district(request.getDistrict())
                .ward(request.getWard())
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
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .district(user.getDistrict())
                .ward(user.getWard())
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
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        // Load user with roles
        user = userRepository.findByIdWithRoles(user.getId()).orElse(user);
        
        // Convert roles to RoleResponse
        java.util.Set<RoleResponse> roleResponses = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleResponses = new java.util.HashSet<>();
            for (Role role : user.getRoles()) {
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
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .district(user.getDistrict())
                .ward(user.getWard())
                .roles(roleResponses)
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Get current user successfully")
                .result(userResponse)
                .build());
    }
    
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getSubject();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        log.info("Updating user: {}", username);
        
        // Update user fields
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            user.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            user.setWard(request.getWard());
        }
        
        user = userRepository.save(user);
        
        // Load user with roles
        user = userRepository.findByIdWithRoles(user.getId()).orElse(user);
        
        // Convert roles to RoleResponse
        java.util.Set<RoleResponse> roleResponses = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleResponses = new java.util.HashSet<>();
            for (Role role : user.getRoles()) {
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
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dob(user.getDob())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .city(user.getCity())
                .district(user.getDistrict())
                .ward(user.getWard())
                .roles(roleResponses)
                .createdAt(user.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("User updated successfully")
                .result(userResponse)
                .build());
    }
}
