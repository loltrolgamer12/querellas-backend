package com.neiva.querillas.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

import com.neiva.querillas.domain.model.Naturaleza;

@Entity
@Table(name = "querella")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Querella {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "radicado_interno", length = 20)
    private String radicadoInterno;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id")
    private Tema tema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Naturaleza naturaleza;   // ← enum

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_asignado_id")
    private Usuario inspectorAsignado;  // Inspector asignado a la querella

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comuna_id")
    private Comuna comuna;

    @Column(name = "id_alcaldia", length = 50)
    private String idAlcaldia;

    @Column(name = "es_migrado", nullable = false)
    private boolean esMigrado = false;

    @Column(name = "creado_por")
    private Long creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_por")
    private Usuario asignadoPor;  // Usuario que asignó el inspector

    @Column(name = "creado_en", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime actualizadoEn;

    @Column(name = "id_local", length = 20)
    private String idLocal;

    @Column(name = "querellante_nombre")
    private String querellanteNombre;

    @Column(name = "querellante_contacto")
    private String querellanteContacto;


    @Column(name = "barrio")
    private String barrio;

    @Column(name = "genero_querellante", length = 20)
    private String generoQuerellante;

    @Column(name = "genero_querellado", length = 20)
    private String generoQuerellado;

    @Column(name = "normas_aplicables", length = 1024)
    private String normasAplicables;

    @Column(name = "observaciones", length = 2048)
    private String observaciones;

    // Flags para informe final
    @Column(name = "tiene_fallo")
    private Boolean tieneFallo;

    @Column(name = "tiene_apelacion")
    private Boolean tieneApelacion;

    @Column(name = "archivado")
    private Boolean archivado;

    @Column(name = "materializacion_medida")
    private Boolean materializacionMedida;


}
