package com.example.visitorpass.dto;

import jakarta.validation.constraints.*;

public record ResidentRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits") String phone,
        @NotBlank(message = "Email is required") @Email(message = "Email is invalid") String email,
        @NotNull(message = "Flat is required") @Positive(message = "Flat id must be positive") Long flatId) {
}
