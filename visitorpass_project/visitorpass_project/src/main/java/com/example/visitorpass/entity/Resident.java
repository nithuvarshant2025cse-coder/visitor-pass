package com.example.visitorpass.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "residents")
@Getter @Setter @NoArgsConstructor
public class Resident {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 120)
    private String email;

    // Many residents can live in one flat; the resident owns the reference.
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;
}
