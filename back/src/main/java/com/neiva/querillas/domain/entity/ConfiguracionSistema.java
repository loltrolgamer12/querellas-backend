package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entidad para almacenar configuraciones del sistema,
 * incluyendo el tracking del round-robin para asignación de querellas
 */
@Entity
@Table(name = "configuracion_sistema")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String clave; // Ejemplo: "ROUND_ROBIN_ULTIMO_INSPECTOR_ID"

    @Column(length = 500)
    private String valor; // Valor como String (puede ser ID, número, JSON, etc.)

    @Column(length = 200)
    private String descripcion;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
