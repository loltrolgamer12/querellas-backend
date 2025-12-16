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
public class QuerellaReporteDTO {

    private Long id;
    private OffsetDateTime fechaRadicado;   // q.creadoEn
    private String inspectorNombre;
    private String radicadoInterno;
    private String temaNombre;
    private String generoQuerellante;
    private String generoQuerellado;
    private String normasAplicables;
    private String barrio;
    private String comunaNombre;

    private Boolean tieneFallo;
    private Boolean tieneApelacion;
    private Boolean archivado;
    private Boolean materializacionMedida;

    private String observaciones;
}
