package com.neiva.querillas.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class HistorialEstadoDTO {
    private String estadoNombre;
    private String motivo;
    private Long usuarioId;
    private OffsetDateTime creadoEn;
}
