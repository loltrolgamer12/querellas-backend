package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO para reporte de despachos comisorios (Excel)
 * Basado en el formato FOR-GGOJ-81
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespachoComitorioReporteDTO {

    // Columnas del reporte según imagen
    private Integer item;
    private OffsetDateTime fechaRecibido;
    private String radicadoProceso;
    private String numeroDespacho;
    private String entidadProcedente;
    private String asunto;
    private String demandanteApoderado;
    private String demandadoApoderado;
    private String inspectorAsignado; // Nombre + zona
    private OffsetDateTime fechaDevolucion;
}
