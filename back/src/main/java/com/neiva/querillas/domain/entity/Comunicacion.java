package com.neiva.querillas.domain.entity;

import com.neiva.querillas.domain.model.EstadoComunicacion;
import com.neiva.querillas.domain.model.TipoComunicacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "comunicaciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comunicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "querella_id", nullable = false)
    private Querella querella;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComunicacion tipo;

    @Column(name = "numero_radicado", length = 50)
    private String numeroRadicado;

    @Column(nullable = false, length = 300)
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio")
    private LocalDate fechaEnvio;

    @Column(nullable = false, length = 200)
    private String destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoComunicacion estado;

    @ManyToOne
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    protected void onCreate() {
        creadoEn = OffsetDateTime.now();
        if (estado == null) {
            estado = EstadoComunicacion.BORRADOR;
        }
    }
}
