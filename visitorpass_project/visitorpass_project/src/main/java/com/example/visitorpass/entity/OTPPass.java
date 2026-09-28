package com.example.visitorpass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_passes")
@Getter @Setter @NoArgsConstructor
public class OTPPass {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String otpCode;

    @Column(nullable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean used = false;

    // Entry time logged by security when the OTP is validated at the gate.
    private LocalDateTime usedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pre_approval_id", nullable = false, unique = true)
    private VisitorPreApproval preApproval;
}
