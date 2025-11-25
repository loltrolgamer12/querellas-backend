package com.neiva.querillas.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CambioEstadoDTO {
  @NotBlank(message = "nuevoEstado es obligatorio")
  private String nuevoEstado; // usa nombres del .sql: RECIBIDA, VERIFICACION, etc.

  @NotBlank(message = "motivo es obligatorio")
  private String motivo;

  // si más adelante usamos seguridad real, esto lo podemos tomar del contexto
  @NotNull(message = "usuarioId es obligatorio")
  private Long usuarioId; // opcional
}
