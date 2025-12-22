package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Notificacion;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.Naturaleza;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.TipoNotificacion;
import com.neiva.querillas.domain.repo.NotificacionRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.ListaNotificacionesResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionService - Tests Unitarios")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificacionService notificacionService;

    private Usuario usuario;
    private Querella querella;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Usuario Test");
        usuario.setEmail("usuario@test.com");
        usuario.setRol(RolUsuario.INSPECTOR);

        querella = new Querella();
        querella.setId(1L);
        querella.setRadicadoInterno("Q-2025-000001");
        querella.setNaturaleza(Naturaleza.PERSONA);

        notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setTitulo("Caso asignado");
        notificacion.setMensaje("Se le ha asignado un nuevo caso");
        notificacion.setTipo(TipoNotificacion.ASIGNACION);
        notificacion.setLeida(false);
        notificacion.setQuerella(querella);
        notificacion.setUsuario(usuario);
        notificacion.setCreadoEn(OffsetDateTime.now());

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========================================
    // TESTS DE LISTAR
    // ========================================

    @Test
    @DisplayName("listar() - Debe listar notificaciones del usuario actual")
    void listar_DebeListarNotificacionesDelUsuarioActual() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        Page<Notificacion> page = new PageImpl<>(List.of(notificacion));
        when(notificacionRepository.findByUsuarioIdAndLeida(eq(1L), any(), any(Pageable.class)))
                .thenReturn(page);
        when(notificacionRepository.countNoLeidasByUsuarioId(1L)).thenReturn(5L);

        // Act
        ListaNotificacionesResponse response = notificacionService.listar(null, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getNoLeidas()).isEqualTo(5);
        verify(notificacionRepository).findByUsuarioIdAndLeida(eq(1L), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("listar() - Debe filtrar por leídas")
    void listar_DebeFiltrarPorLeidas() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        Notificacion leida = new Notificacion();
        leida.setId(2L);
        leida.setTitulo("Notificación leída");
        leida.setLeida(true);
        leida.setUsuario(usuario);

        Page<Notificacion> page = new PageImpl<>(List.of(leida));
        when(notificacionRepository.findByUsuarioIdAndLeida(1L, true, Pageable.ofSize(10)))
                .thenReturn(page);
        when(notificacionRepository.countNoLeidasByUsuarioId(1L)).thenReturn(0L);

        // Act
        ListaNotificacionesResponse response = notificacionService.listar(true, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).allMatch(n -> n.getLeida());
        verify(notificacionRepository).findByUsuarioIdAndLeida(eq(1L), eq(true), any(Pageable.class));
    }

    @Test
    @DisplayName("listar() - Debe filtrar por no leídas")
    void listar_DebeFiltrarPorNoLeidas() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        Page<Notificacion> page = new PageImpl<>(List.of(notificacion));
        when(notificacionRepository.findByUsuarioIdAndLeida(1L, false, Pageable.ofSize(10)))
                .thenReturn(page);
        when(notificacionRepository.countNoLeidasByUsuarioId(1L)).thenReturn(1L);

        // Act
        ListaNotificacionesResponse response = notificacionService.listar(false, 0, 10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getItems()).allMatch(n -> !n.getLeida());
        assertThat(response.getNoLeidas()).isEqualTo(1);
    }

    // ========================================
    // TESTS DE MARCAR COMO LEÍDA
    // ========================================

    @Test
    @DisplayName("marcarComoLeida() - Debe marcar notificación como leída")
    void marcarComoLeida_DebeMarcarNotificacionComoLeida() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        notificacionService.marcarComoLeida(1L);

        // Assert
        verify(notificacionRepository).save(argThat(n -> n.getLeida()));
    }

    @Test
    @DisplayName("marcarComoLeida() - Debe lanzar excepción si notificación no existe")
    void marcarComoLeida_DebeLanzarExcepcionSiNotificacionNoExiste() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificacionService.marcarComoLeida(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Notificación no encontrada");
    }

    @Test
    @DisplayName("marcarComoLeida() - Debe lanzar excepción si no pertenece al usuario")
    void marcarComoLeida_DebeLanzarExcepcionSiNoPertenece() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("auxiliar"); // Usuario ID 2

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(3L);
        notificacion.setUsuario(otroUsuario);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // Act & Assert
        assertThatThrownBy(() -> notificacionService.marcarComoLeida(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No tiene permiso");
    }

    // ========================================
    // TESTS DE MARCAR TODAS COMO LEÍDAS
    // ========================================

    @Test
    @DisplayName("marcarTodasComoLeidas() - Debe marcar todas las notificaciones")
    void marcarTodasComoLeidas_DebeMarcarTodasLasNotificaciones() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.marcarTodasComoLeidas(1L)).thenReturn(5);

        // Act
        Map<String, Integer> result = notificacionService.marcarTodasComoLeidas();

        // Assert
        assertThat(result).containsEntry("marcadas", 5);
        verify(notificacionRepository).marcarTodasComoLeidas(1L);
    }

    @Test
    @DisplayName("marcarTodasComoLeidas() - Debe retornar 0 si no hay notificaciones")
    void marcarTodasComoLeidas_DebeRetornarCeroSiNoHayNotificaciones() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.marcarTodasComoLeidas(1L)).thenReturn(0);

        // Act
        Map<String, Integer> result = notificacionService.marcarTodasComoLeidas();

        // Assert
        assertThat(result).containsEntry("marcadas", 0);
    }

    // ========================================
    // TESTS DE ELIMINAR
    // ========================================

    @Test
    @DisplayName("eliminar() - Debe eliminar notificación")
    void eliminar_DebeEliminarNotificacion() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        doNothing().when(notificacionRepository).delete(any(Notificacion.class));

        // Act
        notificacionService.eliminar(1L);

        // Assert
        verify(notificacionRepository).delete(notificacion);
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si notificación no existe")
    void eliminar_DebeLanzarExcepcionSiNotificacionNoExiste() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");
        when(notificacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificacionService.eliminar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Notificación no encontrada");
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si no pertenece al usuario")
    void eliminar_DebeLanzarExcepcionSiNoPertenece() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("auxiliar"); // Usuario ID 2

        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(3L);
        notificacion.setUsuario(otroUsuario);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        // Act & Assert
        assertThatThrownBy(() -> notificacionService.eliminar(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No tiene permiso");
    }

    // ========================================
    // TESTS DE CREAR NOTIFICACIÓN ASIGNACIÓN
    // ========================================

    @Test
    @DisplayName("crearNotificacionAsignacion() - Debe crear notificación de asignación")
    void crearNotificacionAsignacion_DebeCrearNotificacion() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(10L);
            return n;
        });

        // Act
        notificacionService.crearNotificacionAsignacion(querella, 1L);

        // Assert
        verify(notificacionRepository).save(argThat(n ->
                n.getTitulo().contains("Q-2025-000001") &&
                n.getTipo() == TipoNotificacion.ASIGNACION &&
                !n.getLeida()
        ));
    }

    @Test
    @DisplayName("crearNotificacionAsignacion() - Debe lanzar excepción si usuario no existe")
    void crearNotificacionAsignacion_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> notificacionService.crearNotificacionAsignacion(querella, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ========================================
    // TESTS DE CREAR NOTIFICACIÓN CAMBIO ESTADO
    // ========================================

    @Test
    @DisplayName("crearNotificacionCambioEstado() - Debe crear notificación de cambio de estado")
    void crearNotificacionCambioEstado_DebeCrearNotificacion() {
        // Arrange
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(11L);
            return n;
        });

        // Act
        notificacionService.crearNotificacionCambioEstado(querella, "EN_PROCESO", 1L);

        // Assert
        verify(notificacionRepository).save(argThat(n ->
                n.getTitulo().contains("Q-2025-000001") &&
                n.getMensaje().contains("EN_PROCESO") &&
                n.getTipo() == TipoNotificacion.CAMBIO_ESTADO &&
                !n.getLeida()
        ));
    }

    @Test
    @DisplayName("crearNotificacionCambioEstado() - Debe lanzar excepción si usuario no existe")
    void crearNotificacionCambioEstado_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() ->
                notificacionService.crearNotificacionCambioEstado(querella, "EN_PROCESO", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }
}
