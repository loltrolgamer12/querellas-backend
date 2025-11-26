package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RolUsuario {
    INSPECTOR,
    DIRECTOR,
    AUXILIAR;

    @JsonCreator
    public static RolUsuario fromString(String value) {
        if (value == null) return null;
        return RolUsuario.valueOf(value.toUpperCase());
    }
}
