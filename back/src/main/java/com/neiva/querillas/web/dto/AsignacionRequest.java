package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AsignacionRequest {

    @NotNull(message = "inspeccionId es obligatorio")
    private Long inspeccionId;
}
