package com.example.visitorpass.repository;

import com.example.visitorpass.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    boolean existsByBlockIgnoreCaseAndFlatNumberIgnoreCase(String block, String flatNumber);
}
