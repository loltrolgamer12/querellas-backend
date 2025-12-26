package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.*;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.Naturaleza;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import com.neiva.querillas.domain.repo.*;
import com.neiva.querillas.web.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuerellaService - Tests Unitarios")
class QuerellaServiceTest {

    @Mock
    private QuerellaRepository querellaRepo;

    @Mock
    private EstadoRepository estadoRepo;

    @Mock
    private HistorialEstadoRepository historialRepo;

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private TemaRepository temaRepo;

    @Mock
    private ComunaRepository comunaRepo;

    @Mock
    private EstadoTransicionRepository estadoTransicionRepo;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private QuerellaService querellaService;

    private Tema tema;
    private Comuna comuna;
    private Usuario inspector;
    private Estado estadoRecibida;
    private Querella querella;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        tema = new Tema();
        tema.setId(1L);
        tema.setNombre("Ruido");

        comuna = new Comuna();
        comuna.setId(1L);
        comuna.setNombre("Comuna 1");

        inspector = new Usuario();
        inspector.setId(1L);
        inspector.setNombre("Inspector Test");
        inspector.setRol(RolUsuario.INSPECTOR);
        inspector.setZona(ZonaInspector.NEIVA);
        inspector.setEstado(EstadoUsuario.ACTIVO);

        estadoRecibida = new Estado();
        estadoRecibida.setId(1L);
        estadoRecibida.setModulo("QUERELLA");
        estadoRecibida.setNombre("RECIBIDA");

        querella = new Querella();
        querella.setId(1L);
        querella.setRadicadoInterno("Q-2025-000001");
        querella.setDireccion("Calle 10 #20-30");
        querella.setDescripcion("Ruido excesivo");
        querella.setNaturaleza(Naturaleza.PERSONA);
        querella.setTema(tema);
        querella.setComuna(comuna);
        querella.setInspectorAsignado(inspector);
        querella.setCreadoEn(OffsetDateTime.now());
        querella.setActualizadoEn(OffsetDateTime.now());
    }

    // ========================================
    // TESTS DE CREAR
    // ========================================

    @Test
    @DisplayName("crear() - Debe crear querella exitosamente")
    void crear_DebeCrearQuerellaExitosamente() {
        // Arrange
        QuerellaCreateDTO dto = new QuerellaCreateDTO();
        dto.setNaturaleza(Naturaleza.PERSONA);
        dto.setDireccion("Calle 10 #20-30");
        dto.setDescripcion("Ruido excesivo");
        dto.setTemaId(1L);
        dto.setComunaId(1L);
        dto.setInspectorAsignadoId(1L);
        dto.setBarrio("Centro");

        when(temaRepo.findById(1L)).thenReturn(Optional.of(tema));
        when(comunaRepo.findById(1L)).thenReturn(Optional.of(comuna));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(inspector));
        when(estadoRepo.findByModuloAndNombre("QUERELLA", "RECIBIDA"))
                .thenReturn(Optional.of(estadoRecibida));

        Query mockSeqQuery = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT nextval('seq_radicado_querella')"))
                .thenReturn(mockSeqQuery);
        when(mockSeqQuery.getSingleResult()).thenReturn(1L);

        TypedQuery<Boolean> mockExistsQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Boolean.class)))
                .thenReturn(mockExistsQuery);
        when(mockExistsQuery.setParameter(anyString(), anyString()))
                .thenReturn(mockExistsQuery);
        when(mockExistsQuery.getSingleResult()).thenReturn(false);

        when(querellaRepo.save(any(Querella.class))).thenAnswer(inv -> {
            Querella q = inv.getArgument(0);
            q.setId(1L);
            return q;
        });

        when(historialRepo.save(any(HistorialEstado.class))).thenAnswer(inv -> inv.getArgument(0));

        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        QuerellaResponse response = querellaService.crear(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDireccion()).isEqualTo("Calle 10 #20-30");
        assertThat(response.getDescripcion()).isEqualTo("Ruido excesivo");
        assertThat(response.getNaturaleza()).isEqualTo("PERSONA");

        verify(querellaRepo).save(any(Querella.class));
        verify(historialRepo).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si naturaleza es null")
    void crear_DebeLanzarExcepcionSiNaturaezaEsNull() {
        // Arrange
        QuerellaCreateDTO dto = new QuerellaCreateDTO();
        dto.setNaturaleza(null);
        dto.setDireccion("Calle 10");
        dto.setDescripcion("Test");

        // Act & Assert
        assertThatThrownBy(() -> querellaService.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("naturaleza debe ser");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si tema no existe")
    void crear_DebeLanzarExcepcionSiTemaNoExiste() {
        // Arrange
        QuerellaCreateDTO dto = new QuerellaCreateDTO();
        dto.setNaturaleza(Naturaleza.PERSONA);
        dto.setDireccion("Calle 10");
        dto.setDescripcion("Test");
        dto.setTemaId(999L);

        when(temaRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.crear(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tema no existe");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si comuna no existe")
    void crear_DebeLanzarExcepcionSiComunaNoExiste() {
        // Arrange
        QuerellaCreateDTO dto = new QuerellaCreateDTO();
        dto.setNaturaleza(Naturaleza.PERSONA);
        dto.setDireccion("Calle 10");
        dto.setDescripcion("Test");
        dto.setComunaId(999L);

        when(comunaRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.crear(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comuna no existe");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si estado inicial no existe")
    void crear_DebeLanzarExcepcionSiEstadoInicialNoExiste() {
        // Arrange
        QuerellaCreateDTO dto = new QuerellaCreateDTO();
        dto.setNaturaleza(Naturaleza.PERSONA);
        dto.setDireccion("Calle 10");
        dto.setDescripcion("Test");

        when(estadoRepo.findByModuloAndNombre("QUERELLA", "RECIBIDA"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.crear(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estado inicial RECIBIDA no existe");
    }

    // ========================================
    // TESTS DE DETALLE
    // ========================================

    @Test
    @DisplayName("detalle() - Debe retornar querella por ID")
    void detalle_DebeRetornarQuerellaPorId() {
        // Arrange
        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        QuerellaResponse response = querellaService.detalle(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRadicadoInterno()).isEqualTo("Q-2025-000001");
        verify(querellaRepo).findById(1L);
    }

    @Test
    @DisplayName("detalle() - Debe lanzar excepción si querella no existe")
    void detalle_DebeLanzarExcepcionSiQuerellaNoExiste() {
        // Arrange
        when(querellaRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.detalle(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella no encontrada");
    }

    // ========================================
    // TESTS DE LISTAR BANDEJA
    // ========================================

    @Test
    @DisplayName("listarBandeja() - Debe listar con paginación")
    void listarBandeja_DebeListarConPaginacion() {
        // Arrange
        Query mockDataQuery = mock(Query.class);
        Query mockCountQuery = mock(Query.class);

        when(entityManager.createNativeQuery(contains("SELECT q.*"), eq(Querella.class)))
                .thenReturn(mockDataQuery);
        when(entityManager.createNativeQuery(contains("SELECT COUNT(*)")))
                .thenReturn(mockCountQuery);

        when(mockDataQuery.setParameter(anyString(), any())).thenReturn(mockDataQuery);
        when(mockCountQuery.setParameter(anyString(), any())).thenReturn(mockCountQuery);

        when(mockDataQuery.getResultList()).thenReturn(List.of(querella));
        when(mockCountQuery.getSingleResult()).thenReturn(1L);

        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        PaginaQuerellaResponse response = querellaService.listarBandeja(
                null, null, null, null, null, null, null, 0, 10, null
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("listarBandeja() - Debe aplicar ordenamiento personalizado")
    void listarBandeja_DebeAplicarOrdenamientoPersonalizado() {
        // Arrange
        Query mockDataQuery = mock(Query.class);
        Query mockCountQuery = mock(Query.class);

        when(entityManager.createNativeQuery(contains("q.radicado_interno ASC"), eq(Querella.class)))
                .thenReturn(mockDataQuery);
        when(entityManager.createNativeQuery(contains("SELECT COUNT(*)")))
                .thenReturn(mockCountQuery);

        when(mockDataQuery.setParameter(anyString(), any())).thenReturn(mockDataQuery);
        when(mockCountQuery.setParameter(anyString(), any())).thenReturn(mockCountQuery);

        when(mockDataQuery.getResultList()).thenReturn(List.of());
        when(mockCountQuery.getSingleResult()).thenReturn(0L);

        // Act
        PaginaQuerellaResponse response = querellaService.listarBandeja(
                null, null, null, null, null, null, null, 0, 10, "radicadoInterno,ASC"
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSort()).contains("radicado_interno");
        assertThat(response.getSort()).contains("ASC");
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

        AsignarInspectorDTO dto = new AsignarInspectorDTO();
        dto.setInspectorId(2L);

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(nuevoInspector));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(inspector));
        when(querellaRepo.save(any(Querella.class))).thenAnswer(inv -> inv.getArgument(0));
        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        QuerellaResponse response = querellaService.asignarInspector(1L, dto, 1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getInspectorAsignadoId()).isEqualTo(2L);
        verify(querellaRepo).save(any(Querella.class));
    }

    @Test
    @DisplayName("asignarInspector() - Debe lanzar excepción si querella no existe")
    void asignarInspector_DebeLanzarExcepcionSiQuerellaNoExiste() {
        // Arrange
        AsignarInspectorDTO dto = new AsignarInspectorDTO();
        dto.setInspectorId(2L);

        when(querellaRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.asignarInspector(999L, dto, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella no encontrada");
    }

    @Test
    @DisplayName("asignarInspector() - Debe lanzar excepción si inspector no existe")
    void asignarInspector_DebeLanzarExcepcionSiInspectorNoExiste() {
        // Arrange
        AsignarInspectorDTO dto = new AsignarInspectorDTO();
        dto.setInspectorId(999L);

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(usuarioRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.asignarInspector(1L, dto, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Inspector no encontrado");
    }

    // ========================================
    // TESTS DE CAMBIAR ESTADO
    // ========================================

    @Test
    @DisplayName("cambiarEstado() - Debe cambiar estado exitosamente")
    void cambiarEstado_DebeCambiarEstadoExitosamente() {
        // Arrange
        Estado estadoEnProceso = new Estado();
        estadoEnProceso.setId(2L);
        estadoEnProceso.setModulo("QUERELLA");
        estadoEnProceso.setNombre("EN_PROCESO");

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("EN_PROCESO");
        dto.setMotivo("Iniciando trámite");
        dto.setUsuarioId(1L);

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));
        when(estadoRepo.findByModuloAndNombre("QUERELLA", "RECIBIDA"))
                .thenReturn(Optional.of(estadoRecibida));
        when(estadoRepo.findByModuloAndNombre("QUERELLA", "EN_PROCESO"))
                .thenReturn(Optional.of(estadoEnProceso));
        when(estadoTransicionRepo.existeTransicion("QUERELLA", 1L, 2L))
                .thenReturn(true);
        when(historialRepo.save(any(HistorialEstado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(querellaRepo.save(any(Querella.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        QuerellaResponse response = querellaService.cambiarEstado(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        verify(historialRepo).save(any(HistorialEstado.class));
        verify(querellaRepo).save(any(Querella.class));
    }

    @Test
    @DisplayName("cambiarEstado() - Debe lanzar excepción si motivo está vacío")
    void cambiarEstado_DebeLanzarExcepcionSiMotivoVacio() {
        // Arrange
        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("EN_PROCESO");
        dto.setMotivo("");

        // Act & Assert
        assertThatThrownBy(() -> querellaService.cambiarEstado(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Motivo es obligatorio");
    }

    @Test
    @DisplayName("cambiarEstado() - No debe cambiar si ya está en ese estado")
    void cambiarEstado_NoDebeCambiarSiYaEstaEnEseEstado() {
        // Arrange
        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("RECIBIDA");
        dto.setMotivo("Prueba");

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        QuerellaResponse response = querellaService.cambiarEstado(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        verify(historialRepo, never()).save(any(HistorialEstado.class));
    }

    @Test
    @DisplayName("cambiarEstado() - Debe lanzar excepción si transición no permitida")
    void cambiarEstado_DebeLanzarExcepcionSiTransicionNoPermitida() {
        // Arrange
        Estado estadoCerrada = new Estado();
        estadoCerrada.setId(3L);
        estadoCerrada.setModulo("QUERELLA");
        estadoCerrada.setNombre("CERRADA");

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado("CERRADA");
        dto.setMotivo("Cerrando");

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 1L))
                .thenReturn(Optional.of("RECIBIDA"));
        when(estadoRepo.findByModuloAndNombre("QUERELLA", "RECIBIDA"))
                .thenReturn(Optional.of(estadoRecibida));
        when(estadoRepo.findByModuloAndNombre("QUERELLA", "CERRADA"))
                .thenReturn(Optional.of(estadoCerrada));
        when(estadoTransicionRepo.existeTransicion("QUERELLA", 1L, 3L))
                .thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> querellaService.cambiarEstado(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Transición no permitida");
    }

    // ========================================
    // TESTS DE HISTORIAL ESTADOS
    // ========================================

    @Test
    @DisplayName("historialEstados() - Debe retornar historial completo")
    void historialEstados_DebeRetornarHistorialCompleto() {
        // Arrange
        HistorialEstado he1 = new HistorialEstado();
        he1.setEstado(estadoRecibida);
        he1.setMotivo("apertura");
        he1.setCreadoEn(OffsetDateTime.now().minusDays(2));

        Estado estadoProceso = new Estado();
        estadoProceso.setNombre("EN_PROCESO");

        HistorialEstado he2 = new HistorialEstado();
        he2.setEstado(estadoProceso);
        he2.setMotivo("iniciando trámite");
        he2.setCreadoEn(OffsetDateTime.now());

        when(historialRepo.findByModuloAndCasoIdOrderByCreadoEnDesc("QUERELLA", 1L))
                .thenReturn(List.of(he2, he1));

        // Act
        List<HistorialEstadoDTO> result = querellaService.historialEstados(1L);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEstadoNombre()).isEqualTo("EN_PROCESO");
        assertThat(result.get(1).getEstadoNombre()).isEqualTo("RECIBIDA");
    }

    // ========================================
    // TESTS DE REPORTE TRIMESTRAL
    // ========================================

    @Test
    @DisplayName("generarReporteTrimestral() - Debe generar reporte")
    void generarReporteTrimestral_DebeGenerarReporte() {
        // Arrange
        LocalDate desde = LocalDate.of(2025, 1, 1);
        LocalDate hasta = LocalDate.of(2025, 3, 31);

        TypedQuery<Querella> mockQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Querella.class)))
                .thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of(querella));

        // Act
        List<QuerellaReporteDTO> result = querellaService.generarReporteTrimestral(desde, hasta, null);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRadicadoInterno()).isEqualTo("Q-2025-000001");
    }

    // ========================================
    // TESTS DE DASHBOARD
    // ========================================

    @Test
    @DisplayName("obtenerDashboard() - Debe retornar resumen")
    void obtenerDashboard_DebeRetornarResumen() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(30);
        OffsetDateTime hasta = OffsetDateTime.now();

        Query mockTotalQuery = mock(Query.class);
        Query mockEstadoQuery = mock(Query.class);
        Query mockInspectorQuery = mock(Query.class);
        Query mockNaturalezaQuery = mock(Query.class);

        when(entityManager.createNativeQuery(contains("COUNT(*)"))).thenReturn(mockTotalQuery);
        when(mockTotalQuery.setParameter(anyString(), any())).thenReturn(mockTotalQuery);
        when(mockTotalQuery.getSingleResult()).thenReturn(10L);

        when(entityManager.createNativeQuery(contains("GROUP BY est.estado_nombre")))
                .thenReturn(mockEstadoQuery);
        when(mockEstadoQuery.setParameter(anyString(), any())).thenReturn(mockEstadoQuery);
        when(mockEstadoQuery.getResultList()).thenReturn(List.of(
                new Object[]{"RECIBIDA", 5L},
                new Object[]{"EN_PROCESO", 3L}
        ));

        when(entityManager.createNativeQuery(contains("GROUP BY u.nombre")))
                .thenReturn(mockInspectorQuery);
        when(mockInspectorQuery.setParameter(anyString(), any())).thenReturn(mockInspectorQuery);
        when(mockInspectorQuery.getResultList()).thenReturn(List.of(
                new Object[]{"Inspector Test", 8L}
        ));

        when(entityManager.createNativeQuery(contains("GROUP BY q.naturaleza")))
                .thenReturn(mockNaturalezaQuery);
        when(mockNaturalezaQuery.setParameter(anyString(), any())).thenReturn(mockNaturalezaQuery);
        when(mockNaturalezaQuery.getResultList()).thenReturn(List.of(
                new Object[]{"PERSONA", 6L},
                new Object[]{"OFICIO", 4L}
        ));

        // Act
        DashboardQuerellasResumen result = querellaService.obtenerDashboard(desde, hasta);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalQuerellas()).isEqualTo(10L);
        assertThat(result.getPorEstado()).containsKey("RECIBIDA");
        assertThat(result.getPorInspector()).containsKey("Inspector Test");
        assertThat(result.getPorNaturaleza()).containsKey("PERSONA");
    }

    // ========================================
    // TESTS DE POSIBLES DUPLICADOS
    // ========================================

    @Test
    @DisplayName("posiblesDuplicados() - Debe encontrar candidatos")
    void posiblesDuplicados_DebeEncontrarCandidatos() {
        // Arrange
        Querella duplicado = new Querella();
        duplicado.setId(2L);
        duplicado.setRadicadoInterno("Q-2025-000002");
        duplicado.setDireccion("Calle 10 #20-30");
        duplicado.setCreadoEn(OffsetDateTime.now());

        when(querellaRepo.findById(1L)).thenReturn(Optional.of(querella));
        when(querellaRepo.buscarPosiblesDuplicados(
                eq(1L), anyString(), anyLong(), anyLong(), any(), any()
        )).thenReturn(List.of(duplicado));

        when(historialRepo.findUltimoEstadoNombre("QUERELLA", 2L))
                .thenReturn(Optional.of("RECIBIDA"));

        // Act
        List<QuerellaResponse> result = querellaService.posiblesDuplicados(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("posiblesDuplicados() - Debe lanzar excepción si querella base no existe")
    void posiblesDuplicados_DebeLanzarExcepcionSiQuerellaBaseNoExiste() {
        // Arrange
        when(querellaRepo.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> querellaService.posiblesDuplicados(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella base no encontrada");
    }
}
