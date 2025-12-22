package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("DespachoComitorioRepository - Tests de Integración")
class DespachoComitorioRepositoryTest {

    @Autowired
    private DespachoComitorioRepository despachoRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario inspector;
    private DespachoComisorio despachoPendiente;
    private DespachoComisorio despachoDevuelto;

    @BeforeEach
    void setUp() {
        // Crear inspector
        inspector = Usuario.builder()
                .nombre("Inspector Test")
                .email("inspector@test.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.NEIVA)
                .estado(EstadoUsuario.ACTIVO)
                .build();
        inspector = entityManager.persist(inspector);

        // Crear despacho pendiente
        despachoPendiente = DespachoComisorio.builder()
                .fechaRecibido(OffsetDateTime.now().minusDays(10))
                .numeroDespacho("DC-2025-001")
                .entidadProcedente("Juzgado Civil")
                .asunto("Alimentos")
                .demandanteApoderado("Juan Pérez")
                .demandadoApoderado("María López")
                .inspectorAsignado(inspector)
                .fechaDevolucion(null)
                .build();
        despachoPendiente = entityManager.persist(despachoPendiente);

        // Crear despacho devuelto
        despachoDevuelto = DespachoComisorio.builder()
                .fechaRecibido(OffsetDateTime.now().minusDays(20))
                .numeroDespacho("DC-2025-002")
                .entidadProcedente("Juzgado Penal")
                .asunto("Divorcio")
                .demandanteApoderado("Carlos Ruiz")
                .demandadoApoderado("Ana García")
                .inspectorAsignado(inspector)
                .fechaDevolucion(OffsetDateTime.now().minusDays(5))
                .build();
        despachoDevuelto = entityManager.persist(despachoDevuelto);

        entityManager.flush();
    }

    // ========================================
    // TESTS DE findByNumeroDespacho
    // ========================================

    @Test
    @DisplayName("findByNumeroDespacho() - Debe encontrar despacho por número")
    void findByNumeroDespacho_DebeEncontrarDespachoPorNumero() {
        // Act
        Optional<DespachoComisorio> result = despachoRepository.findByNumeroDespacho("DC-2025-001");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getAsunto()).isEqualTo("Alimentos");
    }

    @Test
    @DisplayName("findByNumeroDespacho() - Debe retornar vacío si no existe")
    void findByNumeroDespacho_DebeRetornarVacioSiNoExiste() {
        // Act
        Optional<DespachoComisorio> result = despachoRepository.findByNumeroDespacho("DC-9999-999");

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS DE findByInspectorAsignadoId
    // ========================================

    @Test
    @DisplayName("findByInspectorAsignadoId() - Debe encontrar despachos del inspector")
    void findByInspectorAsignadoId_DebeEncontrarDespachosDelInspector() {
        // Act
        List<DespachoComisorio> result = despachoRepository.findByInspectorAsignadoId(inspector.getId());

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getInspectorAsignado().getId().equals(inspector.getId()));
    }

    @Test
    @DisplayName("findByInspectorAsignadoId() - Debe retornar vacío si inspector sin despachos")
    void findByInspectorAsignadoId_DebeRetornarVacioSiInspectorSinDespachos() {
        // Act
        List<DespachoComisorio> result = despachoRepository.findByInspectorAsignadoId(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS DE findByEntidadProcedenteContainingIgnoreCase
    // ========================================

    @Test
    @DisplayName("findByEntidadProcedente() - Debe buscar case insensitive")
    void findByEntidadProcedente_DebeBuscarCaseInsensitive() {
        // Act
        List<DespachoComisorio> result = despachoRepository
                .findByEntidadProcedenteContainingIgnoreCase("juzgado civil");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntidadProcedente()).isEqualTo("Juzgado Civil");
    }

    @Test
    @DisplayName("findByEntidadProcedente() - Debe buscar parcialmente")
    void findByEntidadProcedente_DebeBuscarParcialmente() {
        // Act
        List<DespachoComisorio> result = despachoRepository
                .findByEntidadProcedenteContainingIgnoreCase("juzgado");

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    // ========================================
    // TESTS DE findByFechaRecibidoBetween
    // ========================================

    @Test
    @DisplayName("findByFechaRecibidoBetween() - Debe encontrar en rango")
    void findByFechaRecibidoBetween_DebeEncontrarEnRango() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(30);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act
        List<DespachoComisorio> result = despachoRepository.findByFechaRecibidoBetween(desde, hasta);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByFechaRecibidoBetween() - Debe respetar límites del rango")
    void findByFechaRecibidoBetween_DebeRespetarLimitesDelRango() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(15);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act
        List<DespachoComisorio> result = despachoRepository.findByFechaRecibidoBetween(desde, hasta);

        // Assert - Solo debe incluir el despacho más reciente
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumeroDespacho()).isEqualTo("DC-2025-001");
    }

    // ========================================
    // TESTS DE findPendientes
    // ========================================

    @Test
    @DisplayName("findPendientes() - Debe encontrar solo pendientes")
    void findPendientes_DebeEncontrarSoloPendientes() {
        // Act
        List<DespachoComisorio> result = despachoRepository.findPendientes();

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result).allMatch(d -> d.getFechaDevolucion() == null);
    }

    @Test
    @DisplayName("findPendientes() - Debe ordenar por fecha recibido ASC")
    void findPendientes_DebeOrdenarPorFechaRecibidoASC() {
        // Arrange - Crear otro pendiente más reciente
        DespachoComisorio otroPendiente = DespachoComisorio.builder()
                .fechaRecibido(OffsetDateTime.now().minusDays(5))
                .numeroDespacho("DC-2025-003")
                .entidadProcedente("Juzgado Laboral")
                .asunto("Pensión")
                .build();
        entityManager.persist(otroPendiente);
        entityManager.flush();

        // Act
        List<DespachoComisorio> result = despachoRepository.findPendientes();

        // Assert - El más antiguo debe venir primero
        assertThat(result.get(0).getFechaRecibido())
                .isBefore(result.get(result.size() - 1).getFechaRecibido());
    }

    // ========================================
    // TESTS DE findDevueltos
    // ========================================

    @Test
    @DisplayName("findDevueltos() - Debe encontrar solo devueltos")
    void findDevueltos_DebeEncontrarSoloDevueltos() {
        // Act
        List<DespachoComisorio> result = despachoRepository.findDevueltos();

        // Assert
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result).allMatch(d -> d.getFechaDevolucion() != null);
    }

    @Test
    @DisplayName("findDevueltos() - Debe ordenar por fecha devolución DESC")
    void findDevueltos_DebeOrdenarPorFechaDevolucionDESC() {
        // Arrange - Crear otro devuelto más reciente
        DespachoComisorio otroDevuelto = DespachoComisorio.builder()
                .fechaRecibido(OffsetDateTime.now().minusDays(15))
                .numeroDespacho("DC-2025-004")
                .entidadProcedente("Juzgado Administrativo")
                .asunto("Nulidad")
                .fechaDevolucion(OffsetDateTime.now().minusDays(2))
                .build();
        entityManager.persist(otroDevuelto);
        entityManager.flush();

        // Act
        List<DespachoComisorio> result = despachoRepository.findDevueltos();

        // Assert - El más reciente debe venir primero
        assertThat(result.get(0).getFechaDevolucion())
                .isAfter(result.get(result.size() - 1).getFechaDevolucion());
    }

    // ========================================
    // TESTS DE existsByNumeroDespacho
    // ========================================

    @Test
    @DisplayName("existsByNumeroDespacho() - Debe retornar true si existe")
    void existsByNumeroDespacho_DebeRetornarTrueSiExiste() {
        // Act & Assert
        assertThat(despachoRepository.existsByNumeroDespacho("DC-2025-001")).isTrue();
    }

    @Test
    @DisplayName("existsByNumeroDespacho() - Debe retornar false si no existe")
    void existsByNumeroDespacho_DebeRetornarFalseSiNoExiste() {
        // Act & Assert
        assertThat(despachoRepository.existsByNumeroDespacho("DC-9999-999")).isFalse();
    }
}
