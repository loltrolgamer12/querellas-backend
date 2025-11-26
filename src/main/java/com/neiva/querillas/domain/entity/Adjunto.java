package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "adjuntos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Adjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "querella_id", nullable = false)
    private Querella querella;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_archivo", nullable = false, length = 100)
    private String tipoArchivo;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "ruta_storage", nullable = false, length = 500)
    private String rutaStorage; // Ruta donde se guarda el archivo

    @Column(length = 500)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "cargado_por", nullable = false)
    private Usuario cargadoPor;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
    }
}
