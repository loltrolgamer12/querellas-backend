package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Comunicacion;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoComunicacion;
import com.neiva.querillas.domain.model.Naturaleza;
import com.neiva.querillas.domain.model.TipoComunicacion;
import com.neiva.querillas.domain.repo.ComunicacionRepository;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.CambioEstadoComunicacionDTO;
import com.neiva.querillas.web.dto.ComunicacionCreateDTO;
import com.neiva.querillas.web.dto.ComunicacionResponse;
import com.neiva.querillas.web.dto.ComunicacionUpdateDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComunicacionService - Tests Unitarios")
class ComunicacionServiceTest {

    @Mock
    private ComunicacionRepository comunicacionRepository;

    @Mock
    private QuerellaRepository querellaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ComunicacionService comunicacionService;

    private Querella querella;
    private Usuario usuario;
    private Comunicacion comunicacion;

    @BeforeEach
    void setUp() {
        querella = new Querella();
        querella.setId(1L);
        querella.setRadicadoInterno("Q-2025-000001");
        querella.setNaturaleza(Naturaleza.PERSONA);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Usuario Test");

        comunicacion = new Comunicacion();
        comunicacion.setId(1L);
        comunicacion.setQuerella(querella);
        comunicacion.setTipo(TipoComunicacion.OFICIO);
        comunicacion.setNumeroRadicado("RAD-001");
        comunicacion.setAsunto("Asunto de prueba");
        comunicacion.setContenido("Contenido de prueba");
        comunicacion.setDestinatario("Destinatario Test");
        comunicacion.setEstado(EstadoComunicacion.PENDIENTE);
        comunicacion.setCreadoPor(usuario);
        comunicacion.setCreadoEn(OffsetDateTime.now());

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========================================
    // TESTS DE LISTAR
    // ========================================

    @Test
    @DisplayName("listar() - Debe listar comunicaciones de una querella")
    void listar_DebeListarComunicacionesDeUnaQuerella() {
        // Arrange
        when(querellaRepository.existsById(1L)).thenReturn(true);
        when(comunicacionRepository.findByQuerellaIdOrderByCreadoEnDesc(1L))
                .thenReturn(List.of(comunicacion));

        // Act
        List<ComunicacionResponse> result = comunicacionService.listar(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getAsunto()).isEqualTo("Asunto de prueba");
        verify(comunicacionRepository).findByQuerellaIdOrderByCreadoEnDesc(1L);
    }

    @Test
    @DisplayName("listar() - Debe lanzar excepción si querella no existe")
    void listar_DebeLanzarExcepcionSiQuerellaNoExiste() {
        // Arrange
        when(querellaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.listar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella no encontrada");
    }

    @Test
    @DisplayName("listar() - Debe retornar lista vacía si no hay comunicaciones")
    void listar_DebeRetornarListaVaciaSiNoHayComunicaciones() {
        // Arrange
        when(querellaRepository.existsById(1L)).thenReturn(true);
        when(comunicacionRepository.findByQuerellaIdOrderByCreadoEnDesc(1L))
                .thenReturn(List.of());

        // Act
        List<ComunicacionResponse> result = comunicacionService.listar(1L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================
    // TESTS DE CREAR
    // ========================================

    @Test
    @DisplayName("crear() - Debe crear comunicación exitosamente")
    void crear_DebeCrearComunicacionExitosamente() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        ComunicacionCreateDTO dto = new ComunicacionCreateDTO();
        dto.setTipo(TipoComunicacion.OFICIO);
        dto.setNumeroRadicado("RAD-002");
        dto.setAsunto("Nuevo asunto");
        dto.setContenido("Nuevo contenido");
        dto.setDestinatario("Nuevo destinatario");
        dto.setEstado(EstadoComunicacion.PENDIENTE);

        when(querellaRepository.findById(1L)).thenReturn(Optional.of(querella));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comunicacionRepository.save(any(Comunicacion.class))).thenAnswer(inv -> {
            Comunicacion c = inv.getArgument(0);
            c.setId(2L);
            c.setCreadoEn(OffsetDateTime.now());
            return c;
        });

        // Act
        ComunicacionResponse result = comunicacionService.crear(1L, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAsunto()).isEqualTo("Nuevo asunto");
        assertThat(result.getTipo()).isEqualTo(TipoComunicacion.OFICIO);
        verify(comunicacionRepository).save(any(Comunicacion.class));
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si querella no existe")
    void crear_DebeLanzarExcepcionSiQuerellaNoExiste() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        ComunicacionCreateDTO dto = new ComunicacionCreateDTO();
        dto.setTipo(TipoComunicacion.OFICIO);
        dto.setAsunto("Test");

        when(querellaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.crear(999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella no encontrada");
    }

    @Test
    @DisplayName("crear() - Debe lanzar excepción si usuario no existe")
    void crear_DebeLanzarExcepcionSiUsuarioNoExiste() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        ComunicacionCreateDTO dto = new ComunicacionCreateDTO();
        dto.setTipo(TipoComunicacion.OFICIO);
        dto.setAsunto("Test");

        when(querellaRepository.findById(1L)).thenReturn(Optional.of(querella));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.crear(1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ========================================
    // TESTS DE ACTUALIZAR
    // ========================================

    @Test
    @DisplayName("actualizar() - Debe actualizar comunicación exitosamente")
    void actualizar_DebeActualizarComunicacionExitosamente() {
        // Arrange
        ComunicacionUpdateDTO dto = new ComunicacionUpdateDTO();
        dto.setTipo(TipoComunicacion.MEMO);
        dto.setNumeroRadicado("RAD-003");
        dto.setAsunto("Asunto actualizado");
        dto.setContenido("Contenido actualizado");
        dto.setDestinatario("Destinatario actualizado");
        dto.setEstado(EstadoComunicacion.ENVIADO);

        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));
        when(comunicacionRepository.save(any(Comunicacion.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ComunicacionResponse result = comunicacionService.actualizar(1L, 1L, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAsunto()).isEqualTo("Asunto actualizado");
        assertThat(result.getTipo()).isEqualTo(TipoComunicacion.MEMO);
        verify(comunicacionRepository).save(any(Comunicacion.class));
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si comunicación no existe")
    void actualizar_DebeLanzarExcepcionSiComunicacionNoExiste() {
        // Arrange
        ComunicacionUpdateDTO dto = new ComunicacionUpdateDTO();
        when(comunicacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.actualizar(1L, 999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comunicación no encontrada");
    }

    @Test
    @DisplayName("actualizar() - Debe lanzar excepción si comunicación no pertenece a la querella")
    void actualizar_DebeLanzarExcepcionSiComunicacionNoPertenece() {
        // Arrange
        Querella otraQuerella = new Querella();
        otraQuerella.setId(999L);
        comunicacion.setQuerella(otraQuerella);

        ComunicacionUpdateDTO dto = new ComunicacionUpdateDTO();
        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.actualizar(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la querella");
    }

    // ========================================
    // TESTS DE CAMBIAR ESTADO
    // ========================================

    @Test
    @DisplayName("cambiarEstado() - Debe cambiar estado exitosamente")
    void cambiarEstado_DebeCambiarEstadoExitosamente() {
        // Arrange
        CambioEstadoComunicacionDTO dto = new CambioEstadoComunicacionDTO();
        dto.setNuevoEstado(EstadoComunicacion.ENVIADO);

        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));
        when(comunicacionRepository.save(any(Comunicacion.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ComunicacionResponse result = comunicacionService.cambiarEstado(1L, 1L, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo(EstadoComunicacion.ENVIADO);
        verify(comunicacionRepository).save(any(Comunicacion.class));
    }

    @Test
    @DisplayName("cambiarEstado() - Debe lanzar excepción si comunicación no existe")
    void cambiarEstado_DebeLanzarExcepcionSiComunicacionNoExiste() {
        // Arrange
        CambioEstadoComunicacionDTO dto = new CambioEstadoComunicacionDTO();
        dto.setNuevoEstado(EstadoComunicacion.ENVIADO);

        when(comunicacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.cambiarEstado(1L, 999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comunicación no encontrada");
    }

    @Test
    @DisplayName("cambiarEstado() - Debe lanzar excepción si comunicación no pertenece a la querella")
    void cambiarEstado_DebeLanzarExcepcionSiComunicacionNoPertenece() {
        // Arrange
        Querella otraQuerella = new Querella();
        otraQuerella.setId(999L);
        comunicacion.setQuerella(otraQuerella);

        CambioEstadoComunicacionDTO dto = new CambioEstadoComunicacionDTO();
        dto.setNuevoEstado(EstadoComunicacion.ENVIADO);

        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.cambiarEstado(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la querella");
    }

    // ========================================
    // TESTS DE ELIMINAR
    // ========================================

    @Test
    @DisplayName("eliminar() - Debe eliminar comunicación exitosamente")
    void eliminar_DebeEliminarComunicacionExitosamente() {
        // Arrange
        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));
        doNothing().when(comunicacionRepository).delete(any(Comunicacion.class));

        // Act
        comunicacionService.eliminar(1L, 1L);

        // Assert
        verify(comunicacionRepository).delete(comunicacion);
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si comunicación no existe")
    void eliminar_DebeLanzarExcepcionSiComunicacionNoExiste() {
        // Arrange
        when(comunicacionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.eliminar(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comunicación no encontrada");
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si comunicación no pertenece a la querella")
    void eliminar_DebeLanzarExcepcionSiComunicacionNoPertenece() {
        // Arrange
        Querella otraQuerella = new Querella();
        otraQuerella.setId(999L);
        comunicacion.setQuerella(otraQuerella);

        when(comunicacionRepository.findById(1L)).thenReturn(Optional.of(comunicacion));

        // Act & Assert
        assertThatThrownBy(() -> comunicacionService.eliminar(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la querella");
    }
}
