package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "corregimiento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Corregimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
