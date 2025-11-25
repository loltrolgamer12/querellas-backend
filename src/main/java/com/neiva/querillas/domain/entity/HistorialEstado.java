package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_estado")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialEstado {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String modulo; // "QUERELLA" | "DESPACHO"

  @Column(name = "caso_id", nullable = false)
  private Long casoId;   // id de la querella o despacho

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "estado_id", nullable = false)
  private Estado estado;

  @Column(nullable = false)
  private String motivo;

  @Column(name = "usuario_id")
  private Long usuarioId;

  @Column(name = "creado_en", nullable = false)
  private OffsetDateTime creadoEn;
}
