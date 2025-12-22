package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Comuna;
import com.neiva.querillas.domain.entity.Tema;
import com.neiva.querillas.domain.repo.ComunaRepository;
import com.neiva.querillas.domain.repo.TemaRepository;
import com.neiva.querillas.web.dto.CatalogoDTO;
import com.neiva.querillas.web.dto.ItemSimpleDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogoService - Tests Unitarios")
class CatalogoServiceTest {

    @Mock
    private TemaRepository temaRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @InjectMocks
    private CatalogoService catalogoService;

    // ==================== TESTS DE TEMAS ====================

    @Test
    @DisplayName("crearTema() - Debe crear tema exitosamente")
    void crearTema_DebeCrearTemaExitosamente() {
        // Arrange
        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Ruido");

        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNombre("Ruido");

        when(temaRepository.save(any(Tema.class))).thenReturn(tema);

        // Act
        ItemSimpleDTO result = catalogoService.crearTema(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Ruido");
        verify(temaRepository).save(any(Tema.class));
    }

    @Test
    @DisplayName("actualizarTema() - Debe actualizar tema exitosamente")
    void actualizarTema_DebeActualizarTemaExitosamente() {
        // Arrange
        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNombre("Ruido");

        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Ruido Actualizado");

        when(temaRepository.findById(1L)).thenReturn(Optional.of(tema));
        when(temaRepository.save(any(Tema.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemSimpleDTO result = catalogoService.actualizarTema(1L, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Ruido Actualizado");
        verify(temaRepository).save(any(Tema.class));
    }

    @Test
    @DisplayName("actualizarTema() - Debe lanzar excepción si tema no existe")
    void actualizarTema_DebeLanzarExcepcionSiTemaNoExiste() {
        // Arrange
        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Test");

        when(temaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.actualizarTema(999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tema no encontrado");
    }

    @Test
    @DisplayName("eliminarTema() - Debe eliminar tema exitosamente")
    void eliminarTema_DebeEliminarTemaExitosamente() {
        // Arrange
        when(temaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(temaRepository).deleteById(1L);

        // Act
        catalogoService.eliminarTema(1L);

        // Assert
        verify(temaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarTema() - Debe lanzar excepción si tema no existe")
    void eliminarTema_DebeLanzarExcepcionSiTemaNoExiste() {
        // Arrange
        when(temaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.eliminarTema(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tema no encontrado");
    }

    // ==================== TESTS DE COMUNAS ====================

    @Test
    @DisplayName("crearComuna() - Debe crear comuna exitosamente")
    void crearComuna_DebeCrearComunaExitosamente() {
        // Arrange
        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Comuna 1");

        Comuna comuna = new Comuna();
        comuna.setId(1L);
        comuna.setNombre("Comuna 1");

        when(comunaRepository.save(any(Comuna.class))).thenReturn(comuna);

        // Act
        ItemSimpleDTO result = catalogoService.crearComuna(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Comuna 1");
        verify(comunaRepository).save(any(Comuna.class));
    }

    @Test
    @DisplayName("actualizarComuna() - Debe actualizar comuna exitosamente")
    void actualizarComuna_DebeActualizarComunaExitosamente() {
        // Arrange
        Comuna comuna = new Comuna();
        comuna.setId(1L);
        comuna.setNombre("Comuna 1");

        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Comuna 1 Actualizada");

        when(comunaRepository.findById(1L)).thenReturn(Optional.of(comuna));
        when(comunaRepository.save(any(Comuna.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ItemSimpleDTO result = catalogoService.actualizarComuna(1L, dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getNombre()).isEqualTo("Comuna 1 Actualizada");
        verify(comunaRepository).save(any(Comuna.class));
    }

    @Test
    @DisplayName("actualizarComuna() - Debe lanzar excepción si comuna no existe")
    void actualizarComuna_DebeLanzarExcepcionSiComunaNoExiste() {
        // Arrange
        CatalogoDTO dto = new CatalogoDTO();
        dto.setNombre("Test");

        when(comunaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.actualizarComuna(999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comuna no encontrada");
    }

    @Test
    @DisplayName("eliminarComuna() - Debe eliminar comuna exitosamente")
    void eliminarComuna_DebeEliminarComunaExitosamente() {
        // Arrange
        when(comunaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(comunaRepository).deleteById(1L);

        // Act
        catalogoService.eliminarComuna(1L);

        // Assert
        verify(comunaRepository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarComuna() - Debe lanzar excepción si comuna no existe")
    void eliminarComuna_DebeLanzarExcepcionSiComunaNoExiste() {
        // Arrange
        when(comunaRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> catalogoService.eliminarComuna(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comuna no encontrada");
    }
}
