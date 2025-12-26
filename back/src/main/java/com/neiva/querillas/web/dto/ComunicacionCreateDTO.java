package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoComunicacion;
import com.neiva.querillas.domain.model.TipoComunicacion;
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
public class ComunicacionCreateDTO {

    @NotNull(message = "El tipo es obligatorio")
    private TipoComunicacion tipo;

    @Size(max = 50, message = "El número de radicado no puede exceder 50 caracteres")
    private String numeroRadicado;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 300, message = "El asunto no puede exceder 300 caracteres")
    private String asunto;

    private String contenido;

    private LocalDate fechaEnvio;

    @NotBlank(message = "El destinatario es obligatorio")
    @Size(max = 200, message = "El destinatario no puede exceder 200 caracteres")
    private String destinatario;

    @NotNull(message = "El estado es obligatorio")
    private EstadoComunicacion estado;
}
