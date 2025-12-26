package com.neiva.querillas.web.dto;

import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
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
    private ZonaInspector zona;  // NEIVA o CORREGIMIENTO (solo para INSPECTOR)
    private OffsetDateTime creadoEn;
    private OffsetDateTime actualizadoEn;
}
