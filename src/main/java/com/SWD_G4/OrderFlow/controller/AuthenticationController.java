package com.SWD_G4.OrderFlow.controller;

import com.SWD_G4.OrderFlow.dto.request.AuthenticationRequest;
import com.SWD_G4.OrderFlow.dto.request.IntrospectRequest;
import com.SWD_G4.OrderFlow.dto.request.RefreshRequest;
import com.SWD_G4.OrderFlow.dto.response.ApiResponse;
import com.SWD_G4.OrderFlow.dto.response.AuthenticationResponse;
import com.SWD_G4.OrderFlow.dto.response.IntrospectResponse;
import com.SWD_G4.OrderFlow.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;
import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    
    private final AuthenticationService authenticationService;
    
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            
            return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                    .code(1000)
                    .message("Authentication successful")
                    .result(response)
                    .build());
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw e;
        }
    }
    
    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @Valid @RequestBody IntrospectRequest request) throws JOSEException, ParseException {
        try {
            IntrospectResponse response = authenticationService.introspect(request);
            
            return ResponseEntity.ok(ApiResponse.<IntrospectResponse>builder()
                    .code(1000)
                    .message("Token introspection successful")
                    .result(response)
                    .build());
        } catch (Exception e) {
            log.error("Token introspection failed", e);
            throw e;
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization) throws JOSEException, ParseException {
        try {
            String token = authorization.substring(7); // Remove "Bearer " prefix
            authenticationService.logout(token);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Logout successful")
                    .build());
        } catch (Exception e) {
            log.error("Logout failed", e);
            throw e;
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @Valid @RequestBody RefreshRequest request) throws JOSEException, ParseException {
        try {
            AuthenticationResponse response = authenticationService.refresh(request);
            
            return ResponseEntity.ok(ApiResponse.<AuthenticationResponse>builder()
                    .code(1000)
                    .message("Token refresh successful")
                    .result(response)
                    .build());
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw e;
        }
    }
}
