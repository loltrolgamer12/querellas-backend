package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjuntoResponse {

    private Long id;
    private String nombreArchivo;
    private String tipoArchivo;
    private Long tamanoBytes;
    private String descripcion;
    private CargadoPorDTO cargadoPor;
    private OffsetDateTime creadoEn;
    private String url;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CargadoPorDTO {
        private Long id;
        private String nombre;
    }
}
