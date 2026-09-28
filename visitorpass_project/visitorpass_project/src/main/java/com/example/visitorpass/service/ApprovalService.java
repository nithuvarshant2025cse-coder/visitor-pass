package com.example.visitorpass.service;

import com.example.visitorpass.dto.ApprovalRequest;
import com.example.visitorpass.dto.ApprovalResponse;
import com.example.visitorpass.entity.*;
import com.example.visitorpass.exception.BusinessException;
import com.example.visitorpass.exception.ResourceNotFoundException;
import com.example.visitorpass.repository.OtpPassRepository;
import com.example.visitorpass.repository.VisitorPreApprovalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VisitorPreApprovalRepository approvalRepo;
    private final OtpPassRepository otpRepo;
    private final ResidentService residentService;

    public ApprovalService(VisitorPreApprovalRepository approvalRepo, OtpPassRepository otpRepo, ResidentService residentService) {
        this.approvalRepo = approvalRepo;
        this.otpRepo = otpRepo;
        this.residentService = residentService;
    }

    public static ApprovalResponse toResponse(VisitorPreApproval a) {
        OTPPass p = a.getOtpPass();
        Resident r = a.getResident();
        return new ApprovalResponse(a.getId(), r.getId(), r.getName(),
                r.getFlat().getBlock() + "-" + r.getFlat().getFlatNumber(),
                a.getVisitorName(), a.getVisitorPhone(), a.getPurpose(),
                a.getExpectedStart(), a.getExpectedEnd(), a.getStatus(),
                p == null ? null : p.getOtpCode(), p != null && p.isUsed(),
                p == null ? null : p.getUsedAt(), a.getCreatedAt());
    }

    /** Feature 4: auto-expire unused passes once the visit window has passed (runs every minute). */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireOverdue() {
        List<VisitorPreApproval> overdue =
                approvalRepo.findByStatusAndExpectedEndBefore(ApprovalStatus.PENDING, LocalDateTime.now());
        overdue.forEach(a -> a.setStatus(ApprovalStatus.EXPIRED));
        approvalRepo.saveAll(overdue);
    }

    // Features 1 + 2: pre-approve a visitor and generate the linked one-time OTP
    @Transactional
    public ApprovalResponse create(ApprovalRequest r) {
        validateWindow(r);
        VisitorPreApproval a = new VisitorPreApproval();
        a.setResident(residentService.findById(r.residentId()));
        apply(a, r);
        a.setStatus(ApprovalStatus.PENDING);

        OTPPass pass = new OTPPass();
        pass.setOtpCode(generateUniqueOtp());
        pass.setPreApproval(a);
        a.setOtpPass(pass);
        return toResponse(approvalRepo.save(a));
    }

    // Feature 5: view
    @Transactional
    public List<ApprovalResponse> findAll(Long residentId) {
        expireOverdue();
        List<VisitorPreApproval> list = residentId == null
                ? approvalRepo.findAllByOrderByCreatedAtDesc()
                : approvalRepo.findByResidentIdOrderByCreatedAtDesc(residentId);
        return list.stream().map(ApprovalService::toResponse).toList();
    }

    @Transactional
    public ApprovalResponse findOne(Long id) {
        expireOverdue();
        return toResponse(getEntity(id));
    }

    @Transactional
    public ApprovalResponse update(Long id, ApprovalRequest r) {
        expireOverdue();
        VisitorPreApproval a = getEntity(id);
        requirePending(a, "update");
        validateWindow(r);
        if (!a.getResident().getId().equals(r.residentId()))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Resident of an existing pre-approval cannot be changed");
        apply(a, r);
        return toResponse(approvalRepo.save(a));
    }

    // Feature 5: revoke a pending pre-approval before entry
    @Transactional
    public ApprovalResponse revoke(Long id) {
        expireOverdue();
        VisitorPreApproval a = getEntity(id);
        requirePending(a, "revoke");
        a.setStatus(ApprovalStatus.REVOKED);
        return toResponse(approvalRepo.save(a));
    }

    @Transactional
    public void delete(Long id) {
        approvalRepo.delete(getEntity(id));
    }

    private VisitorPreApproval getEntity(Long id) {
        return approvalRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pre-approval not found with id " + id));
    }

    private void requirePending(VisitorPreApproval a, String action) {
        if (a.getStatus() != ApprovalStatus.PENDING)
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Cannot " + action + " a pre-approval that is already " + a.getStatus());
    }

    private void validateWindow(ApprovalRequest r) {
        if (!r.expectedEnd().isAfter(r.expectedStart()))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Expected end must be after expected start");
        if (!r.expectedEnd().isAfter(LocalDateTime.now()))
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Visit window must end in the future");
    }

    private void apply(VisitorPreApproval a, ApprovalRequest r) {
        a.setVisitorName(r.visitorName().trim());
        a.setVisitorPhone(r.visitorPhone());
        a.setPurpose(r.purpose().trim());
        a.setExpectedStart(r.expectedStart());
        a.setExpectedEnd(r.expectedEnd());
    }

    private String generateUniqueOtp() {
        String otp;
        do {
            otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        } while (otpRepo.existsByOtpCode(otp));
        return otp;
    }
}
