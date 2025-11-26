package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoComunicacion {
    BORRADOR,
    ENVIADO,
    RECIBIDO;

    @JsonCreator
    public static EstadoComunicacion fromString(String value) {
        if (value == null) return null;
        return EstadoComunicacion.valueOf(value.toUpperCase());
    }
}
