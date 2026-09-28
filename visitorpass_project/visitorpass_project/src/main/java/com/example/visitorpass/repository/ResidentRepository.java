package com.example.visitorpass.repository;

import com.example.visitorpass.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
    boolean existsByFlatId(Long flatId);
}
