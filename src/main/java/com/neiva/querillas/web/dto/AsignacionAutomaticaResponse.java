package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionAutomaticaResponse {

    private List<QuerellaResponse> querellasAsignadas;
    private Integer totalAsignadas;
    private Long ultimoInspectorId; // ID del último inspector que recibió asignación
}
