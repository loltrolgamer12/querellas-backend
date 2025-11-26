package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoNotificacion {
    ASIGNACION,
    CAMBIO_ESTADO,
    COMENTARIO,
    SISTEMA,
    RECORDATORIO;

    @JsonCreator
    public static TipoNotificacion fromString(String value) {
        if (value == null) return null;
        return TipoNotificacion.valueOf(value.toUpperCase());
    }
}
