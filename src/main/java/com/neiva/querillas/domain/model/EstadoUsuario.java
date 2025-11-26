package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EstadoUsuario {
    ACTIVO,
    BLOQUEADO,
    NO_DISPONIBLE;

    @JsonCreator
    public static EstadoUsuario fromString(String value) {
        if (value == null) return null;
        return EstadoUsuario.valueOf(value.toUpperCase());
    }
}
