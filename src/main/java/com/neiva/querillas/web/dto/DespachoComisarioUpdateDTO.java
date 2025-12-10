package com.neiva.querillas.web.dto;

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
public class DespachoComisarioUpdateDTO {

    private LocalDate fechaRecibido;

    @Size(max = 100, message = "El radicado no puede exceder 100 caracteres")
    private String radicadoProceso;

    @Size(max = 100, message = "El número de despacho no puede exceder 100 caracteres")
    private String numeroDespacho;

    @Size(max = 300, message = "La entidad procedente no puede exceder 300 caracteres")
    private String entidadProcedente;

    @Size(max = 1000, message = "El asunto no puede exceder 1000 caracteres")
    private String asunto;

    @Size(max = 500, message = "El demandante/apoderado no puede exceder 500 caracteres")
    private String demandanteApoderado;

    private Long corregimientoId;

    private LocalDate fechaDevolucion;
}
