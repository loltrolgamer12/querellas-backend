package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import com.neiva.querillas.domain.repo.DespachoComitorioRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.DespachoComitorioCreateDTO;
import com.neiva.querillas.web.dto.DespachoComitorioReporteDTO;
import com.neiva.querillas.web.dto.DespachoComitorioResponse;
import com.neiva.querillas.web.dto.DespachoComitorioUpdateDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DespachoComitorioService - Tests Unitarios")
class DespachoComitorioServiceTest {

    @Mock
    private DespachoComitorioRepository despachoRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @InjectMocks
    private DespachoComitorioService despachoService;

    private Usuario inspector;
    private DespachoComisorio despacho;
    private OffsetDateTime ahora;

    @BeforeEach
    void setUp() {
        ahora = OffsetDateTime.now();

        inspector = new Usuario();
        inspector.setId(1L);
        inspector.setNombre("Inspector Test");
        inspector.setRol(RolUsuario.INSPECTOR);
        inspector.setZona(ZonaInspector.NEIVA);
        inspector.setEstado(EstadoUsuario.ACTIVO);

        despacho = new DespachoComisorio();
        despacho.setId(1L);
        despacho.setFechaRecibido(ahora);
        despacho.setRadicadoProceso("RAD-2025-001");
        despacho.setNumeroDespacho("DC-2025-001");
        despacho.setEntidadProcedente("Juzgado Civil");
        despacho.setAsunto("Alimentos");
        despacho.setDemandanteApoderado("Juan Pérez");
        despacho.setDemandadoApoderado("María López");
        despacho.setInspectorAsignado(inspector);
        despacho.setFechaDevolucion(null);
        despacho.setCreadoEn(ahora);
        despacho.setActualizadoEn(ahora);
    }

    // ========================================
    // TESTS DE CREAR
    // ========================================

    @Test
    @DisplayName("crear() - Debe crear despacho exitosamente")
    void crear_DebeCrearDespachoExitosamente() {
        // Arrange
        DespachoComitorioCreateDTO dto = new DespachoComitorioCreateDTO();
        dto.setFechaRecibido(ahora);
        dto.setNumeroDespacho("DC-2025-001");
        dto.setEntidadProcedente("Juzgado Civil");
        dto.setAsunto("Alimentos");
        dto.setDemandanteApoderado("Juan Pérez");
        dto.setDemandadoApoderado("María López");
        dto.setInspectorAsignadoId(1L);

        when(despachoRepo.existsByNumeroDespacho("DC-2025-001")).thenReturn(false);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(inspector));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> {
            DespachoComisorio d = inv.getArgument(0);
            d.setId(1L);
            d.setCreadoEn(ahora);
            d.setActualizadoEn(ahora);
            return d;
        });

        // Act
        DespachoComitorioResponse response = despachoService.crear(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNumeroDespacho()).isEqualTo("DC-2025-001");
        assertThat(response.getEntidadProcedente()).isEqualTo("Juzgado Civil");
        assertThat(response.getEstado()).isEqualTo("PENDIENTE");

        verify(despachoRepo).existsByNumeroDespacho("DC-2025-001");
        verify(despachoRepo).save(any(DespachoComisorio.class));
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si número despacho ya existe")
    void crear_DebeLanzarExcepcionSiNumeroYaExiste() {
        // Arrange
        DespachoComitorioCreateDTO dto = new DespachoComitorioCreateDTO();
        dto.setNumeroDespacho("DC-2025-001");
        dto.setEntidadProcedente("Juzgado");
        dto.setAsunto("Prueba");

        when(despachoRepo.existsByNumeroDespacho("DC-2025-001")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> despachoService.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un despacho");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si inspector no existe")
    void crear_DebeLanzarExcepcionSiInspectorNoExiste() {
        // Arrange
        DespachoComitorioCreateDTO dto = new DespachoComitorioCreateDTO();
        dto.setNumeroDespacho("DC-2025-001");
        dto.setEntidadProcedente("Juzgado");
        dto.setAsunto("Prueba");
        dto.setInspectorAsignadoId(999L);

        when(despachoRepo.existsByNumeroDespacho("DC-2025-001")).thenReturn(false);
        when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> despachoService.crear(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Inspector no encontrado");
    }

    @Test
    @DisplayName("crear() - Debe crear sin inspector asignado")
    void crear_DebeCrearSinInspector() {
        // Arrange
        DespachoComitorioCreateDTO dto = new DespachoComitorioCreateDTO();
        dto.setFechaRecibido(ahora);
        dto.setNumeroDespacho("DC-2025-001");
        dto.setEntidadProcedente("Juzgado");
        dto.setAsunto("Prueba");

        when(despachoRepo.existsByNumeroDespacho("DC-2025-001")).thenReturn(false);
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> {
            DespachoComisorio d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        // Act
        DespachoComitorioResponse response = despachoService.crear(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getInspectorAsignadoId()).isNull();
    }

    // ========================================
    // TESTS DE OBTENER POR ID
    // ========================================

    @Test
    @DisplayName("obtenerPorId() - Debe retornar despacho por ID")
    void obtenerPorId_DebeRetornarDespachoPorId() {
        // Arrange
        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));

        // Act
        DespachoComitorioResponse response = despachoService.obtenerPorId(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNumeroDespacho()).isEqualTo("DC-2025-001");
    }

    @Test
    @DisplayName("obtenerPorId() - Debe lanzar excepción si no existe")
    void obtenerPorId_DebeLanzarExcepcionSiNoExiste() {
        // Arrange
        when(despachoRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> despachoService.obtenerPorId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Despacho no encontrado");
    }

    // ========================================
    // TESTS DE LISTAR
    // ========================================

    @Test
    @DisplayName("listar() - Debe listar con paginación")
    void listar_DebeListarConPaginacion() {
        // Arrange
        Page<DespachoComisorio> page = new PageImpl<>(List.of(despacho));
        when(despachoRepo.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<DespachoComitorioResponse> response = despachoService.listar(0, 10, "fechaRecibido", "DESC");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listar() - Debe ordenar ascendente")
    void listar_DebeOrdenarAscendente() {
        // Arrange
        Page<DespachoComisorio> page = new PageImpl<>(List.of(despacho));
        when(despachoRepo.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        Page<DespachoComitorioResponse> response = despachoService.listar(0, 10, "numeroDespacho", "ASC");

        // Assert
        assertThat(response).isNotNull();
        verify(despachoRepo).findAll(any(Pageable.class));
    }

    // ========================================
    // TESTS DE LISTAR PENDIENTES
    // ========================================

    @Test
    @DisplayName("listarPendientes() - Debe listar solo pendientes")
    void listarPendientes_DebeListarSoloPendientes() {
        // Arrange
        when(despachoRepo.findPendientes()).thenReturn(List.of(despacho));

        // Act
        List<DespachoComitorioResponse> response = despachoService.listarPendientes();

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getEstado()).isEqualTo("PENDIENTE");
        verify(despachoRepo).findPendientes();
    }

    // ========================================
    // TESTS DE LISTAR DEVUELTOS
    // ========================================

    @Test
    @DisplayName("listarDevueltos() - Debe listar solo devueltos")
    void listarDevueltos_DebeListarSoloDevueltos() {
        // Arrange
        DespachoComisorio despachoDevuelto = new DespachoComisorio();
        despachoDevuelto.setId(2L);
        despachoDevuelto.setNumeroDespacho("DC-2025-002");
        despachoDevuelto.setEntidadProcedente("Juzgado");
        despachoDevuelto.setAsunto("Test");
        despachoDevuelto.setFechaRecibido(ahora);
        despachoDevuelto.setFechaDevolucion(ahora.plusDays(10));

        when(despachoRepo.findDevueltos()).thenReturn(List.of(despachoDevuelto));

        // Act
        List<DespachoComitorioResponse> response = despachoService.listarDevueltos();

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getEstado()).isEqualTo("DEVUELTO");
        verify(despachoRepo).findDevueltos();
    }

    // ========================================
    // TESTS DE LISTAR POR INSPECTOR
    // ========================================

    @Test
    @DisplayName("listarPorInspector() - Debe listar por inspector")
    void listarPorInspector_DebeListarPorInspector() {
        // Arrange
        when(despachoRepo.findByInspectorAsignadoId(1L)).thenReturn(List.of(despacho));

        // Act
        List<DespachoComitorioResponse> response = despachoService.listarPorInspector(1L);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getInspectorAsignadoId()).isEqualTo(1L);
    }

    // ========================================
    // TESTS DE ACTUALIZAR
    // ========================================

    @Test
    @DisplayName("actualizar() - Debe actualizar despacho exitosamente")
    void actualizar_DebeActualizarDespachoExitosamente() {
        // Arrange
        DespachoComitorioUpdateDTO dto = new DespachoComitorioUpdateDTO();
        dto.setFechaRecibido(ahora);
        dto.setNumeroDespacho("DC-2025-001");
        dto.setEntidadProcedente("Juzgado Penal");
        dto.setAsunto("Nuevo Asunto");
        dto.setDemandanteApoderado("Actualizado");
        dto.setDemandadoApoderado("Actualizado");

        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DespachoComitorioResponse response = despachoService.actualizar(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEntidadProcedente()).isEqualTo("Juzgado Penal");
        verify(despachoRepo).save(any(DespachoComisorio.class));
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si nuevo número ya existe")
    void actualizar_DebeLanzarExcepcionSiNuevoNumeroYaExiste() {
        // Arrange
        DespachoComitorioUpdateDTO dto = new DespachoComitorioUpdateDTO();
        dto.setFechaRecibido(ahora);
        dto.setNumeroDespacho("DC-2025-999");
        dto.setEntidadProcedente("Juzgado");
        dto.setAsunto("Test");

        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepo.existsByNumeroDespacho("DC-2025-999")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> despachoService.actualizar(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un despacho");
    }

    @Test
    @DisplayName("actualizar() - Debe permitir actualizar con mismo número")
    void actualizar_DebePermitirActualizarConMismoNumero() {
        // Arrange
        DespachoComitorioUpdateDTO dto = new DespachoComitorioUpdateDTO();
        dto.setFechaRecibido(ahora);
        dto.setNumeroDespacho("DC-2025-001"); // Mismo número
        dto.setEntidadProcedente("Juzgado Actualizado");
        dto.setAsunto("Asunto Actualizado");

        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DespachoComitorioResponse response = despachoService.actualizar(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        verify(despachoRepo, never()).existsByNumeroDespacho(anyString());
    }

    // ========================================
    // TESTS DE ASIGNAR INSPECTOR
    // ========================================

    @Test
    @DisplayName("asignarInspector() - Debe asignar inspector exitosamente")
    void asignarInspector_DebeAsignarInspectorExitosamente() {
        // Arrange
        Usuario nuevoInspector = new Usuario();
        nuevoInspector.setId(2L);
        nuevoInspector.setNombre("Inspector Nuevo");

        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(nuevoInspector));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(inspector));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DespachoComitorioResponse response = despachoService.asignarInspector(1L, 2L, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getInspectorAsignadoId()).isEqualTo(2L);
        verify(despachoRepo).save(any(DespachoComisorio.class));
    }

    @Test
    @DisplayName("asignarInspector() - Debe lanzar excepción si despacho no existe")
    void asignarInspector_DebeLanzarExcepcionSiDespachoNoExiste() {
        // Arrange
        when(despachoRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> despachoService.asignarInspector(999L, 1L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Despacho no encontrado");
    }

    @Test
    @DisplayName("asignarInspector() - Debe lanzar excepción si inspector no existe")
    void asignarInspector_DebeLanzarExcepcionSiInspectorNoExiste() {
        // Arrange
        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> despachoService.asignarInspector(1L, 999L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Inspector no encontrado");
    }

    // ========================================
    // TESTS DE MARCAR COMO DEVUELTO
    // ========================================

    @Test
    @DisplayName("marcarComoDevuelto() - Debe marcar como devuelto con fecha específica")
    void marcarComoDevuelto_DebeMarcarConFechaEspecifica() {
        // Arrange
        OffsetDateTime fechaDevolucion = ahora.plusDays(10);

        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DespachoComitorioResponse response = despachoService.marcarComoDevuelto(1L, fechaDevolucion);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo("DEVUELTO");
        assertThat(response.getFechaDevolucion()).isEqualTo(fechaDevolucion);
    }

    @Test
    @DisplayName("marcarComoDevuelto() - Debe usar fecha actual si no se proporciona")
    void marcarComoDevuelto_DebeUsarFechaActualSiNoSeProvee() {
        // Arrange
        when(despachoRepo.findById(1L)).thenReturn(Optional.of(despacho));
        when(despachoRepo.save(any(DespachoComisorio.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DespachoComitorioResponse response = despachoService.marcarComoDevuelto(1L, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo("DEVUELTO");
        assertThat(response.getFechaDevolucion()).isNotNull();
    }

    // ========================================
    // TESTS DE ELIMINAR
    // ========================================

    @Test
    @DisplayName("eliminar() - Debe eliminar despacho exitosamente")
    void eliminar_DebeEliminarDespachoExitosamente() {
        // Arrange
        when(despachoRepo.existsById(1L)).thenReturn(true);
        doNothing().when(despachoRepo).deleteById(1L);

        // Act
        despachoService.eliminar(1L);

        // Assert
        verify(despachoRepo).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si no existe")
    void eliminar_DebeLanzarExcepcionSiNoExiste() {
        // Arrange
        when(despachoRepo.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> despachoService.eliminar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Despacho no encontrado");
    }

    // ========================================
    // TESTS DE GENERAR REPORTE
    // ========================================

    @Test
    @DisplayName("generarReporte() - Debe generar reporte por rango de fechas")
    void generarReporte_DebeGenerarReportePorRangoDeFechas() {
        // Arrange
        OffsetDateTime desde = ahora.minusDays(30);
        OffsetDateTime hasta = ahora;

        when(despachoRepo.findByFechaRecibidoBetween(desde, hasta))
                .thenReturn(List.of(despacho));

        // Act
        List<DespachoComitorioReporteDTO> result = despachoService.generarReporte(desde, hasta);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumeroDespacho()).isEqualTo("DC-2025-001");
        assertThat(result.get(0).getItem()).isEqualTo(1);
    }

    @Test
    @DisplayName("generarReporte() - Debe incluir información de inspector con zona")
    void generarReporte_DebeIncluirInformacionInspectorConZona() {
        // Arrange
        OffsetDateTime desde = ahora.minusDays(30);
        OffsetDateTime hasta = ahora;

        when(despachoRepo.findByFechaRecibidoBetween(desde, hasta))
                .thenReturn(List.of(despacho));

        // Act
        List<DespachoComitorioReporteDTO> result = despachoService.generarReporte(desde, hasta);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInspectorAsignado()).contains("Inspector Test");
        assertThat(result.get(0).getInspectorAsignado()).contains("NEIVA");
    }
}
