package com.SWD_G4.OrderFlow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    
    // Product related errors
    PRODUCT_NOT_FOUND(2001, "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_AVAILABLE(2002, "Product is not available", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(2003, "Insufficient stock", HttpStatus.BAD_REQUEST),
    
    // Cart related errors
    CART_NOT_FOUND(3001, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(3002, "Cart item not found", HttpStatus.NOT_FOUND),
    
    // Order related errors
    ORDER_NOT_FOUND(4001, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_ALREADY_CONFIRMED(4002, "Order is already confirmed", HttpStatus.BAD_REQUEST),
    EMPTY_CART(4003, "Cart is empty", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}