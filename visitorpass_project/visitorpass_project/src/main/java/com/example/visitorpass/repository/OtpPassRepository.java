package com.example.visitorpass.repository;

import com.example.visitorpass.entity.OTPPass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OtpPassRepository extends JpaRepository<OTPPass, Long> {
    boolean existsByOtpCode(String otpCode);

    // Row lock so two guards cannot validate the same OTP at the same moment.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OTPPass o where o.otpCode = :code")
    Optional<OTPPass> lockByOtpCode(@Param("code") String code);

    List<OTPPass> findByUsedTrueOrderByUsedAtDesc();
}
