package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CambioEstadoRequest {

    /**
     * Código del estado destino (ej: RADICADA, EN_TRAMITE, ARCHIVADA)
     */
    @NotBlank(message = "nuevoEstado es obligatorio")
    private String nuevoEstado;

    @NotBlank(message = "motivo es obligatorio")
    private String motivo;
}
