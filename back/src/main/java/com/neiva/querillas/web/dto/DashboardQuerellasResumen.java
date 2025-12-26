package com.neiva.querillas.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardQuerellasResumen {

    private long totalQuerellas;

    // clave = nombre estado actual (RECIBIDA, VERIFICACION, CERRADA, etc.)
    private Map<String, Long> porEstado;

    // clave = nombre inspector
    private Map<String, Long> porInspector;

    // clave = naturaleza (OFICIO, PERSONA, ANONIMA)
    private Map<String, Long> porNaturaleza;
}
