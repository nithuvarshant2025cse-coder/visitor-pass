package com.example.visitorpass.repository;

import com.example.visitorpass.entity.ApprovalStatus;
import com.example.visitorpass.entity.VisitorPreApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorPreApprovalRepository extends JpaRepository<VisitorPreApproval, Long> {
    List<VisitorPreApproval> findAllByOrderByCreatedAtDesc();
    List<VisitorPreApproval> findByResidentIdOrderByCreatedAtDesc(Long residentId);
    List<VisitorPreApproval> findByStatusAndExpectedEndBefore(ApprovalStatus status, LocalDateTime time);
    boolean existsByResidentId(Long residentId);
}
