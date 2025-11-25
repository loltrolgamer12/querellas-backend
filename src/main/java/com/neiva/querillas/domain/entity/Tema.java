package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "tema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Tema {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable=false)
    private String nombre;
}
