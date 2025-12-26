package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Comuna;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Tema;
import com.neiva.querillas.domain.model.Naturaleza;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("QuerellaRepository - Tests de Integración")
class QuerellaRepositoryTest {

    @Autowired
    private QuerellaRepository querellaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tema tema;
    private Comuna comuna;
    private Querella querella1;
    private Querella querella2;

    @BeforeEach
    void setUp() {
        // Crear tema
        tema = new Tema();
        tema.setNombre("Ruido");
        tema = entityManager.persist(tema);

        // Crear comuna
        comuna = new Comuna();
        comuna.setNombre("Comuna 1");
        comuna = entityManager.persist(comuna);

        // Crear querellas
        querella1 = Querella.builder()
                .radicadoInterno("Q-2025-000001")
                .direccion("Calle 10 #20-30")
                .descripcion("Problema de ruido")
                .naturaleza(Naturaleza.PERSONA)
                .tema(tema)
                .comuna(comuna)
                .creadoEn(OffsetDateTime.now().minusDays(5))
                .actualizadoEn(OffsetDateTime.now().minusDays(5))
                .build();

        querella2 = Querella.builder()
                .radicadoInterno("Q-2025-000002")
                .direccion("Calle 10 #20-30")
                .descripcion("Otro problema de ruido")
                .naturaleza(Naturaleza.OFICIO)
                .tema(tema)
                .comuna(comuna)
                .creadoEn(OffsetDateTime.now().minusDays(2))
                .actualizadoEn(OffsetDateTime.now().minusDays(2))
                .build();

        querella1 = entityManager.persist(querella1);
        querella2 = entityManager.persist(querella2);
        entityManager.flush();
    }

    // ========================================
    // TESTS DE existsByRadicadoInterno
    // ========================================

    @Test
    @DisplayName("existsByRadicadoInterno() - Debe retornar true si existe")
    void existsByRadicadoInterno_DebeRetornarTrueSiExiste() {
        // Act & Assert
        assertThat(querellaRepository.existsByRadicadoInterno("Q-2025-000001")).isTrue();
    }

    @Test
    @DisplayName("existsByRadicadoInterno() - Debe retornar false si no existe")
    void existsByRadicadoInterno_DebeRetornarFalseSiNoExiste() {
        // Act & Assert
        assertThat(querellaRepository.existsByRadicadoInterno("Q-2025-999999")).isFalse();
    }

    // ========================================
    // TESTS DE buscarPosiblesDuplicados
    // ========================================

    @Test
    @DisplayName("buscarPosiblesDuplicados() - Debe encontrar por dirección similar")
    void buscarPosiblesDuplicados_DebeEncontrarPorDireccionSimilar() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(10);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "Calle 10 #20-30",
                comuna.getId(),
                tema.getId(),
                desde,
                hasta
        );

        // Assert
        assertThat(duplicados).hasSize(1);
        assertThat(duplicados.get(0).getId()).isEqualTo(querella2.getId());
    }

    @Test
    @DisplayName("buscarPosiblesDuplicados() - No debe incluir la querella base")
    void buscarPosiblesDuplicados_NoDebeIncluirQuerellaBase() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(10);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "Calle 10 #20-30",
                null,
                null,
                desde,
                hasta
        );

        // Assert
        assertThat(duplicados).noneMatch(q -> q.getId().equals(querella1.getId()));
    }

    @Test
    @DisplayName("buscarPosiblesDuplicados() - Debe filtrar por comuna")
    void buscarPosiblesDuplicados_DebeFiltrarPorComuna() {
        // Arrange
        Comuna otraComuna = new Comuna();
        otraComuna.setNombre("Comuna 2");
        otraComuna = entityManager.persist(otraComuna);

        Querella querella3 = Querella.builder()
                .radicadoInterno("Q-2025-000003")
                .direccion("Calle 10 #20-30")
                .naturaleza(Naturaleza.PERSONA)
                .comuna(otraComuna)
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .build();
        entityManager.persist(querella3);
        entityManager.flush();

        OffsetDateTime desde = OffsetDateTime.now().minusDays(10);
        OffsetDateTime hasta = OffsetDateTime.now().plusDays(1);

        // Act
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "Calle 10 #20-30",
                comuna.getId(), // Filtrar solo por Comuna 1
                null,
                desde,
                hasta
        );

        // Assert
        assertThat(duplicados).hasSize(1);
        assertThat(duplicados.get(0).getComuna().getId()).isEqualTo(comuna.getId());
    }

    @Test
    @DisplayName("buscarPosiblesDuplicados() - Debe filtrar por tema")
    void buscarPosiblesDuplicados_DebeFiltrarPorTema() {
        // Arrange
        Tema otroTema = new Tema();
        otroTema.setNombre("Basuras");
        otroTema = entityManager.persist(otroTema);

        Querella querella3 = Querella.builder()
                .radicadoInterno("Q-2025-000003")
                .direccion("Calle 10 #20-30")
                .naturaleza(Naturaleza.PERSONA)
                .tema(otroTema)
                .comuna(comuna)
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .build();
        entityManager.persist(querella3);
        entityManager.flush();

        OffsetDateTime desde = OffsetDateTime.now().minusDays(10);
        OffsetDateTime hasta = OffsetDateTime.now().plusDays(1);

        // Act
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "Calle 10 #20-30",
                null,
                tema.getId(), // Filtrar solo por tema "Ruido"
                desde,
                hasta
        );

        // Assert
        assertThat(duplicados).hasSize(1);
        assertThat(duplicados.get(0).getTema().getId()).isEqualTo(tema.getId());
    }

    @Test
    @DisplayName("buscarPosiblesDuplicados() - Debe respetar ventana de tiempo")
    void buscarPosiblesDuplicados_DebeRespetarVentanaDeTiempo() {
        // Arrange
        Querella querellaAntigua = Querella.builder()
                .radicadoInterno("Q-2024-000001")
                .direccion("Calle 10 #20-30")
                .naturaleza(Naturaleza.PERSONA)
                .tema(tema)
                .comuna(comuna)
                .creadoEn(OffsetDateTime.now().minusDays(200))
                .actualizadoEn(OffsetDateTime.now().minusDays(200))
                .build();
        entityManager.persist(querellaAntigua);
        entityManager.flush();

        OffsetDateTime desde = OffsetDateTime.now().minusDays(30);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "Calle 10 #20-30",
                null,
                null,
                desde,
                hasta
        );

        // Assert - No debe incluir la querella antigua
        assertThat(duplicados).noneMatch(q -> q.getId().equals(querellaAntigua.getId()));
    }

    @Test
    @DisplayName("buscarPosiblesDuplicados() - Case insensitive en dirección")
    void buscarPosiblesDuplicados_CaseInsensitiveEnDireccion() {
        // Arrange
        OffsetDateTime desde = OffsetDateTime.now().minusDays(10);
        OffsetDateTime hasta = OffsetDateTime.now();

        // Act - Buscar con mayúsculas
        List<Querella> duplicados = querellaRepository.buscarPosiblesDuplicados(
                querella1.getId(),
                "CALLE 10 #20-30",
                null,
                null,
                desde,
                hasta
        );

        // Assert
        assertThat(duplicados).hasSize(1);
    }

    // ========================================
    // TESTS DE findAll y operaciones básicas
    // ========================================

    @Test
    @DisplayName("findAll() - Debe retornar todas las querellas")
    void findAll_DebeRetornarTodasLasQuerellas() {
        // Act
        List<Querella> querellas = querellaRepository.findAll();

        // Assert
        assertThat(querellas).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("findById() - Debe retornar querella por ID")
    void findById_DebeRetornarQuerellaPorId() {
        // Act
        var result = querellaRepository.findById(querella1.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getRadicadoInterno()).isEqualTo("Q-2025-000001");
    }

    @Test
    @DisplayName("save() - Debe guardar nueva querella")
    void save_DebeGuardarNuevaQuerella() {
        // Arrange
        Querella nueva = Querella.builder()
                .radicadoInterno("Q-2025-000099")
                .direccion("Nueva dirección")
                .descripcion("Nueva descripción")
                .naturaleza(Naturaleza.ANONIMA)
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .build();

        // Act
        Querella guardada = querellaRepository.save(nueva);

        // Assert
        assertThat(guardada.getId()).isNotNull();
        assertThat(querellaRepository.findById(guardada.getId())).isPresent();
    }

    @Test
    @DisplayName("delete() - Debe eliminar querella")
    void delete_DebeEliminarQuerella() {
        // Arrange
        Long id = querella1.getId();

        // Act
        querellaRepository.delete(querella1);
        entityManager.flush();

        // Assert
        assertThat(querellaRepository.findById(id)).isEmpty();
    }
}
