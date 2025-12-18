package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.*;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Tests Unitarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private OffsetDateTime ahora;

    @BeforeEach
    void setUp() {
        ahora = OffsetDateTime.now();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan.perez@example.com");
        usuario.setTelefono("3001234567");
        usuario.setPassword("$2a$10$hashedpassword");
        usuario.setRol(RolUsuario.INSPECTOR);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setZona(ZonaInspector.NEIVA);
        usuario.setCreadoEn(ahora);
        usuario.setActualizadoEn(ahora);
    }

    // ========================================
    // TESTS DE LISTAR
    // ========================================

    @Test
    @DisplayName("listar() - Debe listar usuarios con paginación")
    void listar_DebeListarUsuariosConPaginacion() {
        // Arrange
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepository.findAllByRol(any(), any(Pageable.class))).thenReturn(page);

        // Act
        PaginaUsuarioResponse response = usuarioService.listar(null, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(1);
        assertThat(response.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("listar() - Debe filtrar por rol")
    void listar_DebeFiltrarPorRol() {
        // Arrange
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepository.findAllByRol(eq(RolUsuario.INSPECTOR), any(Pageable.class)))
                .thenReturn(page);

        // Act
        PaginaUsuarioResponse response = usuarioService.listar(RolUsuario.INSPECTOR, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getRol()).isEqualTo(RolUsuario.INSPECTOR);
    }

    // ========================================
    // TESTS DE OBTENER POR ID
    // ========================================

    @Test
    @DisplayName("obtenerPorId() - Debe retornar usuario por ID")
    void obtenerPorId_DebeRetornarUsuarioPorId() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act
        UsuarioResponse response = usuarioService.obtenerPorId(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Juan Pérez");
        assertThat(response.getEmail()).isEqualTo("juan.perez@example.com");
    }

    @Test
    @DisplayName("obtenerPorId() - Debe lanzar excepción si no existe")
    void obtenerPorId_DebeLanzarExcepcionSiNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.obtenerPorId(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ========================================
    // TESTS DE CREAR
    // ========================================

    @Test
    @DisplayName("crear() - Debe crear usuario exitosamente")
    void crear_DebeCrearUsuarioExitosamente() {
        // Arrange
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNombre("María García");
        dto.setEmail("maria.garcia@example.com");
        dto.setTelefono("3109876543");
        dto.setPassword("password123");
        dto.setRol(RolUsuario.AUXILIAR);

        when(usuarioRepository.existsByEmail("maria.garcia@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashednewpassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(2L);
            u.setCreadoEn(ahora);
            u.setActualizadoEn(ahora);
            return u;
        });

        // Act
        UsuarioResponse response = usuarioService.crear(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("María García");
        assertThat(response.getEmail()).isEqualTo("maria.garcia@example.com");
        assertThat(response.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);

        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si email ya existe")
    void crear_DebeLanzarExcepcionSiEmailYaExiste() {
        // Arrange
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setEmail("juan.perez@example.com");
        dto.setNombre("Otro Usuario");
        dto.setPassword("password");
        dto.setRol(RolUsuario.AUXILIAR);

        when(usuarioRepository.existsByEmail("juan.perez@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con el email");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si inspector sin zona")
    void crear_DebeLanzarExcepcionSiInspectorSinZona() {
        // Arrange
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNombre("Inspector Sin Zona");
        dto.setEmail("inspector@example.com");
        dto.setPassword("password");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setZona(null);

        when(usuarioRepository.existsByEmail("inspector@example.com")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deben tener una zona asignada");
    }

    @Test
    @DisplayName("crear() - Debe crear inspector con zona")
    void crear_DebeCrearInspectorConZona() {
        // Arrange
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNombre("Inspector Con Zona");
        dto.setEmail("inspector@example.com");
        dto.setPassword("password");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setZona(ZonaInspector.CORREGIMIENTO);

        when(usuarioRepository.existsByEmail("inspector@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(3L);
            u.setCreadoEn(ahora);
            u.setActualizadoEn(ahora);
            return u;
        });

        // Act
        UsuarioResponse response = usuarioService.crear(dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRol()).isEqualTo(RolUsuario.INSPECTOR);
        assertThat(response.getZona()).isEqualTo(ZonaInspector.CORREGIMIENTO);
    }

    // ========================================
    // TESTS DE ACTUALIZAR
    // ========================================

    @Test
    @DisplayName("actualizar() - Debe actualizar usuario exitosamente")
    void actualizar_DebeActualizarUsuarioExitosamente() {
        // Arrange
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Juan Pérez Actualizado");
        dto.setEmail("juan.perez@example.com");
        dto.setTelefono("3001111111");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setEstado(EstadoUsuario.ACTIVO);
        dto.setZona(ZonaInspector.NEIVA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizar(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getNombre()).isEqualTo("Juan Pérez Actualizado");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si usuario no existe")
    void actualizar_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Test");
        dto.setEmail("test@example.com");
        dto.setRol(RolUsuario.AUXILIAR);
        dto.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.actualizar(999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si nuevo email ya existe")
    void actualizar_DebeLanzarExcepcionSiNuevoEmailYaExiste() {
        // Arrange
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Juan Pérez");
        dto.setEmail("otro@example.com");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setEstado(EstadoUsuario.ACTIVO);
        dto.setZona(ZonaInspector.NEIVA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("otro@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.actualizar(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe otro usuario con el email");
    }

    @Test
    @DisplayName("actualizar() - Debe permitir actualizar con mismo email")
    void actualizar_DebePermitirActualizarConMismoEmail() {
        // Arrange
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Juan Pérez Actualizado");
        dto.setEmail("juan.perez@example.com"); // Mismo email
        dto.setTelefono("3001111111");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setEstado(EstadoUsuario.ACTIVO);
        dto.setZona(ZonaInspector.NEIVA);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.actualizar(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        verify(usuarioRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si cambiar a inspector sin zona")
    void actualizar_DebeLanzarExcepcionSiCambiarAInspectorSinZona() {
        // Arrange
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Juan Pérez");
        dto.setEmail("juan.perez@example.com");
        dto.setRol(RolUsuario.INSPECTOR);
        dto.setEstado(EstadoUsuario.ACTIVO);
        dto.setZona(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.actualizar(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deben tener una zona asignada");
    }

    // ========================================
    // TESTS DE CAMBIAR ESTADO
    // ========================================

    @Test
    @DisplayName("cambiarEstado() - Debe cambiar estado exitosamente")
    void cambiarEstado_DebeCambiarEstadoExitosamente() {
        // Arrange
        CambioEstadoUsuarioDTO dto = new CambioEstadoUsuarioDTO();
        dto.setNuevoEstado(EstadoUsuario.BLOQUEADO);
        dto.setMotivo("Suspensión temporal");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UsuarioResponse response = usuarioService.cambiarEstado(1L, dto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstado()).isEqualTo(EstadoUsuario.BLOQUEADO);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarEstado() - Debe lanzar excepción si usuario no existe")
    void cambiarEstado_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        CambioEstadoUsuarioDTO dto = new CambioEstadoUsuarioDTO();
        dto.setNuevoEstado(EstadoUsuario.BLOQUEADO);

        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.cambiarEstado(999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ========================================
    // TESTS DE ELIMINAR
    // ========================================

    @Test
    @DisplayName("eliminar() - Debe marcar usuario como NO_DISPONIBLE")
    void eliminar_DebeMarcarUsuarioComoNoDisponible() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        usuarioService.eliminar(1L);

        // Assert
        verify(usuarioRepository).save(argThat(u ->
                u.getEstado() == EstadoUsuario.NO_DISPONIBLE
        ));
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si usuario no existe")
    void eliminar_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> usuarioService.eliminar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ========================================
    // TESTS DE LISTAR INSPECTORES
    // ========================================

    @Test
    @DisplayName("listarInspectores() - Debe listar todos los inspectores activos")
    void listarInspectores_DebeListarTodosLosInspectoresActivos() {
        // Arrange
        Usuario inspector2 = new Usuario();
        inspector2.setId(2L);
        inspector2.setNombre("Inspector 2");
        inspector2.setRol(RolUsuario.INSPECTOR);
        inspector2.setEstado(EstadoUsuario.ACTIVO);
        inspector2.setZona(ZonaInspector.CORREGIMIENTO);

        when(usuarioRepository.findByRolAndEstado(RolUsuario.INSPECTOR, EstadoUsuario.ACTIVO))
                .thenReturn(List.of(usuario, inspector2));

        // Act
        List<UsuarioResponse> response = usuarioService.listarInspectores(null);

        // Assert
        assertThat(response).hasSize(2);
        assertThat(response).allMatch(u -> u.getRol() == RolUsuario.INSPECTOR);
    }

    @Test
    @DisplayName("listarInspectores() - Debe filtrar por zona NEIVA")
    void listarInspectores_DebeFiltrarPorZonaNeiva() {
        // Arrange
        when(usuarioRepository.findByRolAndZonaAndEstado(
                RolUsuario.INSPECTOR, ZonaInspector.NEIVA, EstadoUsuario.ACTIVO
        )).thenReturn(List.of(usuario));

        // Act
        List<UsuarioResponse> response = usuarioService.listarInspectores(ZonaInspector.NEIVA);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getZona()).isEqualTo(ZonaInspector.NEIVA);
    }

    @Test
    @DisplayName("listarInspectores() - Debe filtrar por zona CORREGIMIENTO")
    void listarInspectores_DebeFiltrarPorZonaCorregimiento() {
        // Arrange
        Usuario inspector2 = new Usuario();
        inspector2.setId(2L);
        inspector2.setNombre("Inspector 2");
        inspector2.setRol(RolUsuario.INSPECTOR);
        inspector2.setEstado(EstadoUsuario.ACTIVO);
        inspector2.setZona(ZonaInspector.CORREGIMIENTO);
        inspector2.setEmail("inspector2@example.com");

        when(usuarioRepository.findByRolAndZonaAndEstado(
                RolUsuario.INSPECTOR, ZonaInspector.CORREGIMIENTO, EstadoUsuario.ACTIVO
        )).thenReturn(List.of(inspector2));

        // Act
        List<UsuarioResponse> response = usuarioService.listarInspectores(ZonaInspector.CORREGIMIENTO);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getZona()).isEqualTo(ZonaInspector.CORREGIMIENTO);
    }

    @Test
    @DisplayName("listarInspectores() - No debe incluir inspectores bloqueados")
    void listarInspectores_NoDebeIncluirInspectoresBloqueados() {
        // Arrange
        when(usuarioRepository.findByRolAndEstado(RolUsuario.INSPECTOR, EstadoUsuario.ACTIVO))
                .thenReturn(List.of(usuario));

        // Act
        List<UsuarioResponse> response = usuarioService.listarInspectores(null);

        // Assert
        assertThat(response).allMatch(u -> u.getEstado() == EstadoUsuario.ACTIVO);
    }
}
