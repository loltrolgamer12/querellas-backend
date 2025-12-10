package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DespachoComisarioCreateDTO {

    @NotNull(message = "La fecha de recibido es obligatoria")
    private LocalDate fechaRecibido;

    @NotBlank(message = "El radicado del proceso es obligatorio")
    @Size(max = 100, message = "El radicado no puede exceder 100 caracteres")
    private String radicadoProceso;

    @NotBlank(message = "El número de despacho es obligatorio")
    @Size(max = 100, message = "El número de despacho no puede exceder 100 caracteres")
    private String numeroDespacho;

    @NotBlank(message = "La entidad procedente es obligatoria")
    @Size(max = 300, message = "La entidad procedente no puede exceder 300 caracteres")
    private String entidadProcedente;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 1000, message = "El asunto no puede exceder 1000 caracteres")
    private String asunto;

    @Size(max = 500, message = "El demandante/apoderado no puede exceder 500 caracteres")
    private String demandanteApoderado;

    private Long corregimientoId;

    private LocalDate fechaDevolucion;
}
