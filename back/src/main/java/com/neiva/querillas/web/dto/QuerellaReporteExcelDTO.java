package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO para reporte trimestral en Excel
 * Columnas según formato de la alcaldía
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuerellaReporteExcelDTO {

    // FECHA DE RADICADO
    private OffsetDateTime fechaRadicado;

    // INSPECTOR
    private String inspectorNombre;
    private String inspectorZona;  // NEIVA o CORREGIMIENTO

    // RADICADO DEL PROCESO
    private String radicadoInterno;
    private String idLocal;

    // ASUNTO/TEMA
    private String temaNombre;
    private String descripcion;

    // GÉNERO QUERELLANTE
    private String generoQuerellante;  // M, F, OTRO

    // GÉNERO QUERELLADO
    private String generoQuerellado;   // M, F, OTRO

    // NORMAS APLICABLES
    private String normasAplicables;

    // BARRIO
    private String barrio;

    // COMUNA
    private String comunaNombre;

    // ESTADO DEL PROCESO
    private String estadoActual;

    // MATERIALIZACIÓN DE LA MEDIDA
    private Boolean materializacionMedida;  // SI/NO

    // OBSERVACIONES
    private String observaciones;

    // Campos adicionales para el reporte
    private Boolean tieneFallo;
    private Boolean tieneApelacion;
    private Boolean archivado;
}
