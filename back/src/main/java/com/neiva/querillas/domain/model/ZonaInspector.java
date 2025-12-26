package com.neiva.querillas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Zona de cobertura del inspector
 */
public enum ZonaInspector {
    NEIVA,              // Casco urbano de Neiva
    CORREGIMIENTO;      // Corregimientos de Neiva

    @JsonCreator
    public static ZonaInspector fromString(String value) {
        if (value == null) return null;
        return ZonaInspector.valueOf(value.toUpperCase());
    }
}
