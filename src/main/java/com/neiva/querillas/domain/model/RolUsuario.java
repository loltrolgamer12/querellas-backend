package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RolUsuario {
    INSPECTOR("Inspector de Convivencia y Paz"),
    DIRECTOR("Director"),
    AUXILIAR("Auxiliar");

    private final String nombreLegible;

    RolUsuario(String nombreLegible) {
        this.nombreLegible = nombreLegible;
    }

    public String getNombreLegible() {
        return nombreLegible;
    }

    @JsonCreator
    public static RolUsuario fromString(String value) {
        if (value == null) return null;
        return RolUsuario.valueOf(value.toUpperCase());
    }
}
