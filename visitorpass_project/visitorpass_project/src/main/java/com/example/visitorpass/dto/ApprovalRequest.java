package com.example.visitorpass.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record ApprovalRequest(
        @NotNull(message = "Resident is required") @Positive(message = "Resident id must be positive") Long residentId,
        @NotBlank(message = "Visitor name is required") String visitorName,
        @NotBlank(message = "Visitor phone is required")
        @Pattern(regexp = "\\d{10}", message = "Visitor phone must be 10 digits") String visitorPhone,
        @NotBlank(message = "Purpose is required") String purpose,
        @NotNull(message = "Expected start is required") LocalDateTime expectedStart,
        @NotNull(message = "Expected end is required") LocalDateTime expectedEnd) {
}
