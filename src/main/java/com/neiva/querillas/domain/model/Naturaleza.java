package com.neiva.querillas.domain.model;

public enum Naturaleza { OFICIO, PERSONA, ANONIMA

    ;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Naturaleza from(String raw) {
        if (raw == null) return null;
        return Naturaleza.valueOf(raw.trim().toUpperCase());
    }


}