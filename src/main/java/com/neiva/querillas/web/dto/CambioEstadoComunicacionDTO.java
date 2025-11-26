package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoComunicacion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoComunicacionDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoComunicacion nuevoEstado;
}
