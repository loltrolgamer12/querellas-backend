package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("UsuarioRepository - Tests de Integración")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByEmail() - Debe encontrar usuario por email")
    void findByEmail_DebeEncontrarUsuarioPorEmail() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nombre("Test Usuario")
                .email("test@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.AUXILIAR)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        entityManager.persist(usuario);
        entityManager.flush();

        // Act
        Optional<Usuario> result = usuarioRepository.findByEmail("test@example.com");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Test Usuario");
    }

    @Test
    @DisplayName("existsByEmail() - Debe retornar true si email existe")
    void existsByEmail_DebeRetornarTrueSiEmailExiste() {
        // Arrange
        Usuario usuario = Usuario.builder()
                .nombre("Test")
                .email("exists@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.AUXILIAR)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        entityManager.persist(usuario);
        entityManager.flush();

        // Act & Assert
        assertThat(usuarioRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(usuarioRepository.existsByEmail("noexiste@example.com")).isFalse();
    }

    @Test
    @DisplayName("findAllByRol() - Debe filtrar por rol")
    void findAllByRol_DebeFiltrarPorRol() {
        // Arrange
        Usuario inspector1 = Usuario.builder()
                .nombre("Inspector 1")
                .email("inspector1@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.NEIVA)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario inspector2 = Usuario.builder()
                .nombre("Inspector 2")
                .email("inspector2@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.CORREGIMIENTO)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario auxiliar = Usuario.builder()
                .nombre("Auxiliar")
                .email("auxiliar@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.AUXILIAR)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        entityManager.persist(inspector1);
        entityManager.persist(inspector2);
        entityManager.persist(auxiliar);
        entityManager.flush();

        // Act
        Page<Usuario> inspectores = usuarioRepository.findAllByRol(
                RolUsuario.INSPECTOR,
                PageRequest.of(0, 10)
        );

        // Assert
        assertThat(inspectores.getContent()).hasSize(2);
        assertThat(inspectores.getContent()).allMatch(u -> u.getRol() == RolUsuario.INSPECTOR);
    }

    @Test
    @DisplayName("findByRolAndEstado() - Debe filtrar por rol y estado")
    void findByRolAndEstado_DebeFiltrarPorRolYEstado() {
        // Arrange
        Usuario activo = Usuario.builder()
                .nombre("Inspector Activo")
                .email("activo@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.NEIVA)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario bloqueado = Usuario.builder()
                .nombre("Inspector Bloqueado")
                .email("bloqueado@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.NEIVA)
                .estado(EstadoUsuario.BLOQUEADO)
                .build();

        entityManager.persist(activo);
        entityManager.persist(bloqueado);
        entityManager.flush();

        // Act
        List<Usuario> activos = usuarioRepository.findByRolAndEstado(
                RolUsuario.INSPECTOR,
                EstadoUsuario.ACTIVO
        );

        // Assert
        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("findByRolAndZonaAndEstado() - Debe filtrar por rol, zona y estado")
    void findByRolAndZonaAndEstado_DebeFiltrarPorRolZonaYEstado() {
        // Arrange
        Usuario neivaActivo = Usuario.builder()
                .nombre("Inspector Neiva Activo")
                .email("neiva@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.NEIVA)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        Usuario corregimientoActivo = Usuario.builder()
                .nombre("Inspector Corregimiento Activo")
                .email("corregimiento@example.com")
                .password("$2a$10$hash")
                .rol(RolUsuario.INSPECTOR)
                .zona(ZonaInspector.CORREGIMIENTO)
                .estado(EstadoUsuario.ACTIVO)
                .build();

        entityManager.persist(neivaActivo);
        entityManager.persist(corregimientoActivo);
        entityManager.flush();

        // Act
        List<Usuario> neivaInspectores = usuarioRepository.findByRolAndZonaAndEstado(
                RolUsuario.INSPECTOR,
                ZonaInspector.NEIVA,
                EstadoUsuario.ACTIVO
        );

        // Assert
        assertThat(neivaInspectores).hasSize(1);
        assertThat(neivaInspectores.get(0).getZona()).isEqualTo(ZonaInspector.NEIVA);
    }
}
