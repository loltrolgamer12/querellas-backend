package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoUsuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoUsuarioDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoUsuario nuevoEstado;

    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;
}
