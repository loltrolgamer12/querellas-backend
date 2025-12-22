package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespachoComitorioCreateDTO {

    @NotNull(message = "La fecha de recibido es obligatoria")
    private OffsetDateTime fechaRecibido;

    @Size(max = 50, message = "El radicado del proceso no puede exceder 50 caracteres")
    private String radicadoProceso;

    @NotBlank(message = "El número de despacho es obligatorio")
    @Size(max = 50, message = "El número de despacho no puede exceder 50 caracteres")
    private String numeroDespacho;

    @NotBlank(message = "La entidad procedente es obligatoria")
    @Size(max = 255, message = "La entidad procedente no puede exceder 255 caracteres")
    private String entidadProcedente;

    @NotBlank(message = "El asunto es obligatorio")
    private String asunto;

    private String demandanteApoderado;
    private String demandadoApoderado;

    private Long inspectorAsignadoId;
    private Long asignadoPorId;

    private OffsetDateTime fechaDevolucion;
    private String observaciones;
}
