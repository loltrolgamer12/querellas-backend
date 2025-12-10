package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "despacho_comisorio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DespachoComisorio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_recibido", nullable = false)
    private LocalDate fechaRecibido;

    @Column(name = "radicado_proceso", nullable = false, length = 100)
    private String radicadoProceso;

    @Column(name = "numero_despacho", nullable = false, length = 100)
    private String numeroDespacho;

    @Column(name = "entidad_procedente", nullable = false, length = 300)
    private String entidadProcedente;

    @Column(name = "asunto", nullable = false, length = 1000)
    private String asunto;

    @Column(name = "demandante_apoderado", length = 500)
    private String demandanteApoderado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corregimiento_id")
    private Corregimiento corregimientoAsignado;

    @Column(name = "fecha_devolucion")
    private LocalDate fechaDevolucion;

    @Column(name = "creado_por")
    private Long creadoPor;

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
