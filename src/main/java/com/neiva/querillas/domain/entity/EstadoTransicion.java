package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_transicion",
       uniqueConstraints = @UniqueConstraint(
         name="estado_transicion_modulo_desde_estado_id_hacia_estado_id_key",
         columnNames = {"modulo","desde_estado_id","hacia_estado_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EstadoTransicion {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String modulo; // "QUERELLA" | "DESPACHO"

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "desde_estado_id", nullable = false)
  private Estado desde;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hacia_estado_id", nullable = false)
  private Estado hacia;
}
