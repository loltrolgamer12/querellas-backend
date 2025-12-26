package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoComunicacion;
import com.neiva.querillas.domain.model.TipoComunicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComunicacionResponse {

    private Long id;
    private TipoComunicacion tipo;
    private String numeroRadicado;
    private String asunto;
    private String contenido;
    private LocalDate fechaEnvio;
    private String destinatario;
    private EstadoComunicacion estado;
    private CreadoPorDTO creadoPor;
    private OffsetDateTime creadoEn;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreadoPorDTO {
        private Long id;
        private String nombre;
    }
}
