package com.example.visitorpass.dto;

import com.example.visitorpass.entity.ApprovalStatus;

import java.time.LocalDateTime;

public record ApprovalResponse(
        Long id, Long residentId, String residentName, String flat,
        String visitorName, String visitorPhone, String purpose,
        LocalDateTime expectedStart, LocalDateTime expectedEnd,
        ApprovalStatus status, String otpCode, boolean otpUsed,
        LocalDateTime entryTime, LocalDateTime createdAt) {
}
