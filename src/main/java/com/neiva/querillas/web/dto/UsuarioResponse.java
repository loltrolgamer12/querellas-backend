package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private RolUsuario rol;
    private EstadoUsuario estado;
    private Long inspeccionId;
    private String inspeccionNombre;
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
}
