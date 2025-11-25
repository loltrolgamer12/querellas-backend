package com.neiva.querillas.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class QuerellaResponse {

    private Long id;

    private String radicadoInterno;  // q.radicado_interno
    private String idLocal;          // q.id_local (puede ser null si aún no se asigna inspección)

    private String direccion;
    private String descripcion;
    private String naturaleza;

    // Tema
    private Long temaId;
    private String temaNombre;

    // Inspección
    private Long inspeccionId;
    private String inspeccionNombre;

    // Comuna
    private Long comunaId;
    private String comunaNombre;

    // Estado actual (derivado del historial_estado más reciente)
    private String estadoActual;

    // Marca temporal para orden / auditoría
    private OffsetDateTime creadoEn;



       // NUEVOS CAMPOS
       private String barrio;
       private String generoQuerellante;
       private String generoQuerellado;
       private String normasAplicables;
       private String observaciones;
   
       private Boolean tieneFallo;
       private Boolean tieneApelacion;
       private Boolean archivado;
       private Boolean materializacionMedida;
}
