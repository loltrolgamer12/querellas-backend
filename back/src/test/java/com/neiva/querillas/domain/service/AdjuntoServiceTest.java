package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Adjunto;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.repo.AdjuntoRepository;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.AdjuntoResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdjuntoService - Tests Unitarios")
class AdjuntoServiceTest {

    @Mock
    private AdjuntoRepository adjuntoRepository;

    @Mock
    private QuerellaRepository querellaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile archivo;

    @InjectMocks
    private AdjuntoService adjuntoService;

    @TempDir
    Path tempDir;

    private Querella querella;
    private Usuario usuario;
    private Adjunto adjunto;

    @BeforeEach
    void setUp() {
        querella = new Querella();
        querella.setId(1L);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Usuario Test");

        adjunto = new Adjunto();
        adjunto.setId(1L);
        adjunto.setQuerella(querella);
        adjunto.setNombreArchivo("test.pdf");
        adjunto.setTipoArchivo("application/pdf");
        adjunto.setTamanoBytes(1024L);
        adjunto.setRutaStorage(tempDir.resolve("test.pdf").toString());
        adjunto.setCargadoPor(usuario);

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Setup storage path
        ReflectionTestUtils.setField(adjuntoService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(adjuntoService, "maxFileSize", 10485760L);
    }

    // ========================================
    // TESTS DE LISTAR
    // ========================================

    @Test
    @DisplayName("listar() - Debe listar adjuntos de una querella")
    void listar_DebeListarAdjuntosDeUnaQuerella() {
        // Arrange
        when(querellaRepository.existsById(1L)).thenReturn(true);
        when(adjuntoRepository.findByQuerellaIdOrderByCreadoEnDesc(1L))
                .thenReturn(List.of(adjunto));

        // Act
        List<AdjuntoResponse> result = adjuntoService.listar(1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombreArchivo()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("listar() - Debe lanzar excepción si querella no existe")
    void listar_DebeLanzarExcepcionSiQuerellaNoExiste() {
        // Arrange
        when(querellaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> adjuntoService.listar(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Querella no encontrada");
    }

    // ========================================
    // TESTS DE SUBIR
    // ========================================

    @Test
    @DisplayName("subir() - Debe subir archivo exitosamente")
    void subir_DebeSubirArchivoExitosamente() throws IOException {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        when(querellaRepository.findById(1L)).thenReturn(Optional.of(querella));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(inv -> {
            Adjunto a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        when(archivo.getOriginalFilename()).thenReturn("test.pdf");
        when(archivo.getContentType()).thenReturn("application/pdf");
        when(archivo.getSize()).thenReturn(1024L);
        when(archivo.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        // Act
        AdjuntoResponse result = adjuntoService.subir(1L, archivo, "Descripción test");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombreArchivo()).isEqualTo("test.pdf");
        verify(adjuntoRepository).save(any(Adjunto.class));
    }

    @Test
    @DisplayName("subir() - Debe lanzar excepción si archivo es muy grande")
    void subir_DebeLanzarExcepcionSiArchivoMuyGrande() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        when(querellaRepository.findById(1L)).thenReturn(Optional.of(querella));
        when(archivo.getSize()).thenReturn(20 * 1024 * 1024L); // 20MB

        // Act & Assert
        assertThatThrownBy(() -> adjuntoService.subir(1L, archivo, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("excede el tamaño máximo");
    }

    @Test
    @DisplayName("subir() - Debe lanzar excepción si tipo no permitido")
    void subir_DebeLanzarExcepcionSiTipoNoPermitido() {
        // Arrange
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("directora");

        when(querellaRepository.findById(1L)).thenReturn(Optional.of(querella));
        when(archivo.getSize()).thenReturn(1024L);
        when(archivo.getContentType()).thenReturn("application/zip");

        // Act & Assert
        assertThatThrownBy(() -> adjuntoService.subir(1L, archivo, "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de archivo no permitido");
    }

    // ========================================
    // TESTS DE DESCARGAR
    // ========================================

    @Test
    @DisplayName("descargar() - Debe lanzar excepción si adjunto no pertenece a querella")
    void descargar_DebeLanzarExcepcionSiAdjuntoNoPertenece() {
        // Arrange
        Querella otraQuerella = new Querella();
        otraQuerella.setId(999L);
        adjunto.setQuerella(otraQuerella);

        when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

        // Act & Assert
        assertThatThrownBy(() -> adjuntoService.descargar(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la querella");
    }

    // ========================================
    // TESTS DE ELIMINAR
    // ========================================

    @Test
    @DisplayName("eliminar() - Debe eliminar adjunto exitosamente")
    void eliminar_DebeEliminarAdjuntoExitosamente() throws IOException {
        // Arrange
        Path archivoPath = tempDir.resolve("test-file.pdf");
        java.nio.file.Files.write(archivoPath, "test content".getBytes());
        adjunto.setRutaStorage(archivoPath.toString());

        when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));
        doNothing().when(adjuntoRepository).delete(any(Adjunto.class));

        // Act
        adjuntoService.eliminar(1L, 1L);

        // Assert
        verify(adjuntoRepository).delete(adjunto);
        assertThat(archivoPath).doesNotExist();
    }

    @Test
    @DisplayName("eliminar() - Debe lanzar excepción si adjunto no pertenece a querella")
    void eliminar_DebeLanzarExcepcionSiAdjuntoNoPertenece() {
        // Arrange
        Querella otraQuerella = new Querella();
        otraQuerella.setId(999L);
        adjunto.setQuerella(otraQuerella);

        when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

        // Act & Assert
        assertThatThrownBy(() -> adjuntoService.eliminar(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece a la querella");
    }

    @Test
    @DisplayName("obtenerPorId() - Debe retornar adjunto por ID")
    void obtenerPorId_DebeRetornarAdjuntoPorId() {
        // Arrange
        when(adjuntoRepository.findById(1L)).thenReturn(Optional.of(adjunto));

        // Act
        Adjunto result = adjuntoService.obtenerPorId(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
}
