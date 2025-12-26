package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "despacho_comisorio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespachoComisorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_recibido", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime fechaRecibido;

    @Column(name = "radicado_proceso", length = 50)
    private String radicadoProceso;

    @Column(name = "numero_despacho", length = 50, nullable = false)
    private String numeroDespacho;

    @Column(name = "entidad_procedente", nullable = false)
    private String entidadProcedente;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String asunto;

    @Column(name = "demandante_apoderado", columnDefinition = "TEXT")
    private String demandanteApoderado;

    @Column(name = "demandado_apoderado", columnDefinition = "TEXT")
    private String demandadoApoderado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_asignado_id")
    private Usuario inspectorAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por")
    private Usuario asignadoPor;

    @Column(name = "fecha_devolucion", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime fechaDevolucion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "creado_en", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (creadoEn == null) {
            creadoEn = now;
        }
        if (actualizadoEn == null) {
            actualizadoEn = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}
