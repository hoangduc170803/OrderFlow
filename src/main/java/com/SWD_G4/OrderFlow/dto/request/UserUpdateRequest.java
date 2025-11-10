package com.SWD_G4.OrderFlow.dto.request;

import java.time.LocalDate;

import com.SWD_G4.OrderFlow.validator.DobConstraint;
import jakarta.validation.constraints.Email;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Email(message = "INVALID_EMAIL")
    String email;

    String firstName;
    String lastName;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;

    String phoneNumber;
    String address;
    String city;
    String district;
    String ward;
}

