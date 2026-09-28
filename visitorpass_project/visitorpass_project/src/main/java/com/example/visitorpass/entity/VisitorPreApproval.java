package com.example.visitorpass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_pre_approvals")
@Getter @Setter @NoArgsConstructor
public class VisitorPreApproval {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One resident can create many pre-approvals.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @Column(nullable = false, length = 100)
    private String visitorName;

    @Column(nullable = false, length = 15)
    private String visitorPhone;

    @Column(nullable = false, length = 255)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime expectedStart;

    @Column(nullable = false)
    private LocalDateTime expectedEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApprovalStatus status = ApprovalStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // One approval has exactly one OTP pass.
    @OneToOne(mappedBy = "preApproval", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OTPPass otpPass;
}
