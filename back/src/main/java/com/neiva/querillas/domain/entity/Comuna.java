package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "comuna")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Comuna {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre", nullable=false)
    private String nombre;
}
