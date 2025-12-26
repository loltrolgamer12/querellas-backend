package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespachoComitorioResponse {

    private Long id;
    private OffsetDateTime fechaRecibido;
    private String radicadoProceso;
    private String numeroDespacho;
    private String entidadProcedente;
    private String asunto;
    private String demandanteApoderado;
    private String demandadoApoderado;

    // Inspector asignado
    private Long inspectorAsignadoId;
    private String inspectorAsignadoNombre;
    private String inspectorAsignadoZona;

    // Asignado por
    private Long asignadoPorId;
    private String asignadoPorNombre;

    private OffsetDateTime fechaDevolucion;
    private String observaciones;

    // Estado calculado
    private String estado; // PENDIENTE, DEVUELTO

    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
}
