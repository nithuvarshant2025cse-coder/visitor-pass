package com.example.visitorpass.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "flats", uniqueConstraints = @UniqueConstraint(columnNames = {"block_name", "flat_number"}))
@Getter @Setter @NoArgsConstructor
public class Flat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Block is required")
    @Column(name = "block_name", nullable = false, length = 20)
    private String block;

    @NotBlank(message = "Flat number is required")
    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;
}
