package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespachoComisarioResponse {

    private Long id;
    private LocalDate fechaRecibido;
    private String radicadoProceso;
    private String numeroDespacho;
    private String entidadProcedente;
    private String asunto;
    private String demandanteApoderado;
    private Long corregimientoId;
    private String corregimientoNombre;
    private LocalDate fechaDevolucion;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
}
