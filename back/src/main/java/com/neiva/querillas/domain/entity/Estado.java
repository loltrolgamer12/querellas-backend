package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "estado",
       uniqueConstraints = @UniqueConstraint(name="estado_modulo_nombre_key",
         columnNames = {"modulo","nombre"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estado {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false) private String modulo; // QUERELLA|DESPACHO
  @Column(nullable = false) private String nombre; // RECIBIDA, VERIFICACION...
  @Column(name="creado_en", nullable = false) private OffsetDateTime creadoEn;
}
