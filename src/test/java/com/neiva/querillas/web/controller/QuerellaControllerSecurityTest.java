package com.neiva.querillas.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neiva.querillas.domain.model.Naturaleza;           
import com.neiva.querillas.domain.service.QuerellaService;
import com.neiva.querillas.security.TestSecurityConfig;
import com.neiva.querillas.web.dto.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.neiva.querillas.security.JwtAuthFilter;


import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = QuerellaController.class)

@Import(com.neiva.querillas.security.TestSecurityConfig.class)
class QuerellaControllerSecurityTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @MockBean JwtAuthFilter jwtAuthFilter; 
  @MockBean QuerellaService service;

  // ------ helpers
  private String bodyCrearValido() throws Exception {
    var dto = new QuerellaCreateDTO();
    dto.setDireccion("Cra 10 # 12-34");
    dto.setDescripcion("Ruido todas las noches");
    dto.setNaturaleza(Naturaleza.OFICIO);      // <-- CAMBIO: enum, no String
    dto.setTemaId(1L);
    dto.setComunaId(1L);
    dto.setInspeccionId(1L);
    return om.writeValueAsString(dto);         // Jackson serializa el enum como "OFICIO"
  }

  // ===== Crear: DIRECTORA y AUXILIAR -> 200; INSPECTOR -> 403
  @Test @WithMockUser(username="dir", roles={"DIRECTORA"})
  void crear_ok_directora() throws Exception {
    Mockito.when(service.crear(any())).thenReturn(QuerellaResponse.builder().id(1L).build());
    mvc.perform(post("/api/querellas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bodyCrearValido()))
      .andExpect(status().isOk());
  }

  @Test @WithMockUser(username="aux", roles={"AUXILIAR"})
  void crear_ok_auxiliar() throws Exception {
    Mockito.when(service.crear(any())).thenReturn(QuerellaResponse.builder().id(2L).build());
    mvc.perform(post("/api/querellas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bodyCrearValido()))
      .andExpect(status().isOk());
  }

  @Test @WithMockUser(username="insp", roles={"INSPECTOR"})
  void crear_forbidden_inspector() throws Exception {
    mvc.perform(post("/api/querellas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bodyCrearValido()))
      .andExpect(status().isForbidden());
  }

  // ===== Validación: naturaleza vacía -> 400
  @Test @WithMockUser(username="dir", roles={"DIRECTORA"})
  void crear_badRequest_por_validacion() throws Exception {
    var dto = new QuerellaCreateDTO();
    dto.setDireccion("A");
    dto.setDescripcion("B");
    // no seteamos naturaleza => vacío/omiso
    mvc.perform(post("/api/querellas")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.fields.naturaleza").exists());
  }

  // ===== Asignar inspección: DIRECTORA / INSPECTOR -> 200; AUXILIAR -> 403
  @Test @WithMockUser(username="insp", roles={"INSPECTOR"})
  void asignar_ok_inspector() throws Exception {
    var dto = new AsignarInspeccionDTO();
    dto.setInspeccionId(5L);
    Mockito.when(service.asignarInspeccion(eq(1L), any())).thenReturn(QuerellaResponse.builder().id(1L).build());

    mvc.perform(put("/api/querellas/1/inspeccion")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
      .andExpect(status().isOk());
  }

  @Test @WithMockUser(username="aux", roles={"AUXILIAR"})
  void asignar_forbidden_auxiliar() throws Exception {
    var dto = new AsignarInspeccionDTO();
    dto.setInspeccionId(5L);
    mvc.perform(put("/api/querellas/1/inspeccion")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
      .andExpect(status().isForbidden());
  }

  // ===== Cambiar estado: DIRECTORA / INSPECTOR -> 200; AUXILIAR -> 403
  @Test @WithMockUser(username="dir", roles={"DIRECTORA"})
  void cambiarEstado_ok_directora() throws Exception {
    var dto = new CambioEstadoDTO();
    dto.setNuevoEstado("ASIGNADA");
    dto.setMotivo("Prueba");
    dto.setUsuarioId(99L);
    Mockito.when(service.cambiarEstado(eq(1L), any())).thenReturn(QuerellaResponse.builder().id(1L).build());

    mvc.perform(put("/api/querellas/1/estado")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
      .andExpect(status().isOk());
  }

  @Test @WithMockUser(username="aux", roles={"AUXILIAR"})
  void cambiarEstado_forbidden_auxiliar() throws Exception {
    var dto = new CambioEstadoDTO();
    dto.setNuevoEstado("ASIGNADA");
    dto.setMotivo("Prueba");
    dto.setUsuarioId(99L);

    mvc.perform(put("/api/querellas/1/estado")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(dto)))
      .andExpect(status().isForbidden());
  }

  // ===== GETs abiertos a los 3 roles
  @Test @WithMockUser(username="insp", roles={"INSPECTOR"})
  void detalle_ok_roles_lectura() throws Exception {
    Mockito.when(service.detalle(7L)).thenReturn(QuerellaResponse.builder().id(7L).build());
    mvc.perform(get("/api/querellas/7")).andExpect(status().isOk());
  }

  @Test @WithMockUser(username="aux", roles={"AUXILIAR"})
  void historial_ok_roles_lectura() throws Exception {
    Mockito.when(service.historialEstados(7L)).thenReturn(
        List.of(new HistorialEstadoDTO("RECIBIDA","apertura", null, OffsetDateTime.now())));
    mvc.perform(get("/api/querellas/7/historial")).andExpect(status().isOk());
  }
}
