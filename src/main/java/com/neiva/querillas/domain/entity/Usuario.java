package com.neiva.querillas.domain.entity;

import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String password; // Hasheado con BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUsuario estado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ZonaInspector zona; // NEIVA o CORREGIMIENTO (solo para INSPECTOR)

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
        if (estado == null) {
            estado = EstadoUsuario.ACTIVO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
