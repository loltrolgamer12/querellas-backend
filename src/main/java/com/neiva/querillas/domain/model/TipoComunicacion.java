package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoComunicacion {
    OFICIO,
    NOTIFICACION,
    CITACION,
    AUTO,
    RESOLUCION;

    @JsonCreator
    public static TipoComunicacion fromString(String value) {
        if (value == null) return null;
        return TipoComunicacion.valueOf(value.toUpperCase());
    }
}
