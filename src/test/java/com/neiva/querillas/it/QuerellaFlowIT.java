package com.neiva.querillas.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neiva.querillas.domain.model.Naturaleza;              
import com.neiva.querillas.web.dto.AsignarInspeccionDTO;
import com.neiva.querillas.web.dto.CambioEstadoDTO;
import com.neiva.querillas.web.dto.QuerellaCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // desactiva filtros de seguridad en este IT
@ActiveProfiles("test")
@Sql(scripts = "/sql/testdata.sql")
class QuerellaFlowIT {

  @Container
  static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("testdb")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void ds(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", pg::getJdbcUrl);
    r.add("spring.datasource.username", pg::getUsername);
    r.add("spring.datasource.password", pg::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  String crearQuerella() throws Exception {
    var dto = new QuerellaCreateDTO();
    dto.setDireccion("Cra 10 # 12-34");
    dto.setDescripcion("Ruido todas las noches");
    dto.setNaturaleza(Naturaleza.OFICIO);      // <-- CAMBIO: enum, no String
    dto.setTemaId(1L);
    dto.setComunaId(1L);
    dto.setInspeccionId(1L);

    var res = mvc.perform(post("/api/querellas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estadoActual").value("RECIBIDA"))
        .andReturn();

    JsonNode node = om.readTree(res.getResponse().getContentAsString());
    return node.get("id").asText();
  }

  @Test
  void flujoCompleto() throws Exception {
    // 1) crear
    var id = crearQuerella();

    // 2) historial contiene RECIBIDA
    mvc.perform(get("/api/querellas/{id}/historial", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estadoNombre").value("RECIBIDA"));

    // 3) asignar inspección
    var asg = new AsignarInspeccionDTO();
    asg.setInspeccionId(1L);
    mvc.perform(put("/api/querellas/{id}/inspeccion", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(asg)))
        .andExpect(status().isOk());

    // 4) cambiar estado a ASIGNADA
    var ce = new CambioEstadoDTO();
    ce.setNuevoEstado("ASIGNADA");
    ce.setMotivo("auto-test");
    ce.setUsuarioId(1L);
    mvc.perform(put("/api/querellas/{id}/estado", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(ce)))
        .andExpect(status().isOk());

    // 5) historial ahora con ASIGNADA primero
    mvc.perform(get("/api/querellas/{id}/historial", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].estadoNombre").value("ASIGNADA"));
  }
}
