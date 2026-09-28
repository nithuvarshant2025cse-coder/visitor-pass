package com.example.visitorpass.service;

import com.example.visitorpass.dto.ApprovalResponse;
import com.example.visitorpass.entity.*;
import com.example.visitorpass.exception.BusinessException;
import com.example.visitorpass.repository.OtpPassRepository;
import com.example.visitorpass.repository.VisitorPreApprovalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GateService {
    private final OtpPassRepository otpRepo;
    private final VisitorPreApprovalRepository approvalRepo;

    public GateService(OtpPassRepository otpRepo, VisitorPreApprovalRepository approvalRepo) {
        this.otpRepo = otpRepo;
        this.approvalRepo = approvalRepo;
    }

    /**
     * Feature 3: security validates the OTP at the gate and the entry time is logged.
     * Every business rule is checked BEFORE anything is saved.
     * noRollbackFor keeps the EXPIRED status when a late OTP is rejected.
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public ApprovalResponse validate(String otp) {
        OTPPass pass = otpRepo.lockByOtpCode(otp)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Invalid OTP"));
        VisitorPreApproval a = pass.getPreApproval();
        LocalDateTime now = LocalDateTime.now();

        // Rule 1: an OTP can be successfully validated for entry only once
        if (pass.isUsed() || a.getStatus() == ApprovalStatus.USED)
            throw new BusinessException(HttpStatus.CONFLICT, "OTP has already been used for entry");
        // Rule 2: revoked or expired approvals never allow entry, even with a valid-looking OTP
        if (a.getStatus() == ApprovalStatus.REVOKED)
            throw new BusinessException(HttpStatus.FORBIDDEN, "Pre-approval was revoked by the resident");
        if (a.getStatus() == ApprovalStatus.EXPIRED)
            throw new BusinessException(HttpStatus.FORBIDDEN, "Pre-approval has expired");
        if (now.isAfter(a.getExpectedEnd())) {
            a.setStatus(ApprovalStatus.EXPIRED);
            approvalRepo.save(a);
            throw new BusinessException(HttpStatus.FORBIDDEN, "Pre-approval has expired");
        }
        if (now.isBefore(a.getExpectedStart()))
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Visit window has not started yet (valid from " + a.getExpectedStart().toString().replace('T', ' ') + ")");

        pass.setUsed(true);
        pass.setUsedAt(now);
        a.setStatus(ApprovalStatus.USED);
        otpRepo.save(pass);
        approvalRepo.save(a);
        return ApprovalService.toResponse(a);
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> entryLog() {
        return otpRepo.findByUsedTrueOrderByUsedAtDesc().stream()
                .map(p -> ApprovalService.toResponse(p.getPreApproval())).toList();
    }
}
