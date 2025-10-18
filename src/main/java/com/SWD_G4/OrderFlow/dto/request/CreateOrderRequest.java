package com.SWD_G4.OrderFlow.dto.request;

import com.SWD_G4.OrderFlow.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private String notes;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
