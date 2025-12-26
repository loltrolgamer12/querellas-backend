package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionAutomaticaRequest {

    @NotEmpty(message = "La lista de querellas no puede estar vacía")
    private List<Long> querellaIds;

    @NotNull(message = "El ID del usuario que asigna es obligatorio")
    private Long asignadoPorId; // ID de la directora que ejecuta la asignación
}
