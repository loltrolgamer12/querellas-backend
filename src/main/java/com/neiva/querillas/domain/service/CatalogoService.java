package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Barrio;
import com.neiva.querillas.domain.entity.Comuna;
import com.neiva.querillas.domain.entity.Corregimiento;
import com.neiva.querillas.domain.entity.Inspeccion;
import com.neiva.querillas.domain.entity.Tema;
import com.neiva.querillas.domain.repo.BarrioRepository;
import com.neiva.querillas.domain.repo.ComunaRepository;
import com.neiva.querillas.domain.repo.CorregimientoRepository;
import com.neiva.querillas.domain.repo.InspeccionRepository;
import com.neiva.querillas.domain.repo.TemaRepository;
import com.neiva.querillas.web.dto.CatalogoDTO;
import com.neiva.querillas.web.dto.ItemSimpleDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoService {

    private final InspeccionRepository inspeccionRepository;
    private final TemaRepository temaRepository;
    private final ComunaRepository comunaRepository;
    private final BarrioRepository barrioRepository;
    private final CorregimientoRepository corregimientoRepository;

    // ==================== INSPECCIONES ====================

    /**
     * Crear inspección
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO crearInspeccion(CatalogoDTO dto) {
        log.info("Creando inspección - nombre: {}", dto.getNombre());

        Inspeccion inspeccion = new Inspeccion();
        inspeccion.setNombre(dto.getNombre());

        inspeccion = inspeccionRepository.save(inspeccion);
        log.info("Inspección creada con ID: {}", inspeccion.getId());

        return new ItemSimpleDTO(inspeccion.getId(), inspeccion.getNombre());
    }

    /**
     * Actualizar inspección
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO actualizarInspeccion(Long id, CatalogoDTO dto) {
        log.info("Actualizando inspección ID: {}", id);

        Inspeccion inspeccion = inspeccionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inspección no encontrada con ID: " + id));

        inspeccion.setNombre(dto.getNombre());
        inspeccion = inspeccionRepository.save(inspeccion);

        log.info("Inspección {} actualizada", id);

        return new ItemSimpleDTO(inspeccion.getId(), inspeccion.getNombre());
    }

    /**
     * Eliminar inspección
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminarInspeccion(Long id) {
        log.info("Eliminando inspección ID: {}", id);

        if (!inspeccionRepository.existsById(id)) {
            throw new EntityNotFoundException("Inspección no encontrada con ID: " + id);
        }

        inspeccionRepository.deleteById(id);
        log.info("Inspección {} eliminada", id);
    }

    // ==================== TEMAS ====================

    /**
     * Crear tema
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO crearTema(CatalogoDTO dto) {
        log.info("Creando tema - nombre: {}", dto.getNombre());

        Tema tema = new Tema();
        tema.setNombre(dto.getNombre());

        tema = temaRepository.save(tema);
        log.info("Tema creado con ID: {}", tema.getId());

        return new ItemSimpleDTO(tema.getId(), tema.getNombre());
    }

    /**
     * Actualizar tema
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO actualizarTema(Long id, CatalogoDTO dto) {
        log.info("Actualizando tema ID: {}", id);

        Tema tema = temaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tema no encontrado con ID: " + id));

        tema.setNombre(dto.getNombre());
        tema = temaRepository.save(tema);

        log.info("Tema {} actualizado", id);

        return new ItemSimpleDTO(tema.getId(), tema.getNombre());
    }

    /**
     * Eliminar tema
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminarTema(Long id) {
        log.info("Eliminando tema ID: {}", id);

        if (!temaRepository.existsById(id)) {
            throw new EntityNotFoundException("Tema no encontrado con ID: " + id);
        }

        temaRepository.deleteById(id);
        log.info("Tema {} eliminado", id);
    }

    // ==================== COMUNAS ====================

    /**
     * Crear comuna
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO crearComuna(CatalogoDTO dto) {
        log.info("Creando comuna - nombre: {}", dto.getNombre());

        Comuna comuna = new Comuna();
        comuna.setNombre(dto.getNombre());

        comuna = comunaRepository.save(comuna);
        log.info("Comuna creada con ID: {}", comuna.getId());

        return new ItemSimpleDTO(comuna.getId(), comuna.getNombre());
    }

    /**
     * Actualizar comuna
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO actualizarComuna(Long id, CatalogoDTO dto) {
        log.info("Actualizando comuna ID: {}", id);

        Comuna comuna = comunaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comuna no encontrada con ID: " + id));

        comuna.setNombre(dto.getNombre());
        comuna = comunaRepository.save(comuna);

        log.info("Comuna {} actualizada", id);

        return new ItemSimpleDTO(comuna.getId(), comuna.getNombre());
    }

    /**
     * Eliminar comuna
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminarComuna(Long id) {
        log.info("Eliminando comuna ID: {}", id);

        if (!comunaRepository.existsById(id)) {
            throw new EntityNotFoundException("Comuna no encontrada con ID: " + id);
        }

        comunaRepository.deleteById(id);
        log.info("Comuna {} eliminada", id);
    }

    // ==================== BARRIOS ====================

    /**
     * Crear barrio
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO crearBarrio(CatalogoDTO dto) {
        log.info("Creando barrio - nombre: {}", dto.getNombre());

        Barrio barrio = Barrio.builder()
                .nombre(dto.getNombre())
                .build();

        // Si se proporciona comuna_id, asociar el barrio
        if (dto.getComunaId() != null) {
            Comuna comuna = comunaRepository.findById(dto.getComunaId())
                    .orElseThrow(() -> new EntityNotFoundException("Comuna no encontrada con ID: " + dto.getComunaId()));
            barrio.setComuna(comuna);
        }

        barrio = barrioRepository.save(barrio);
        log.info("Barrio creado con ID: {}", barrio.getId());

        return new ItemSimpleDTO(barrio.getId(), barrio.getNombre());
    }

    /**
     * Actualizar barrio
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO actualizarBarrio(Long id, CatalogoDTO dto) {
        log.info("Actualizando barrio ID: {}", id);

        Barrio barrio = barrioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Barrio no encontrado con ID: " + id));

        barrio.setNombre(dto.getNombre());

        // Actualizar comuna si se proporciona
        if (dto.getComunaId() != null) {
            Comuna comuna = comunaRepository.findById(dto.getComunaId())
                    .orElseThrow(() -> new EntityNotFoundException("Comuna no encontrada con ID: " + dto.getComunaId()));
            barrio.setComuna(comuna);
        }

        barrio = barrioRepository.save(barrio);

        log.info("Barrio {} actualizado", id);

        return new ItemSimpleDTO(barrio.getId(), barrio.getNombre());
    }

    /**
     * Eliminar barrio
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminarBarrio(Long id) {
        log.info("Eliminando barrio ID: {}", id);

        if (!barrioRepository.existsById(id)) {
            throw new EntityNotFoundException("Barrio no encontrado con ID: " + id);
        }

        barrioRepository.deleteById(id);
        log.info("Barrio {} eliminado", id);
    }

    // ==================== CORREGIMIENTOS ====================

    /**
     * Crear corregimiento
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO crearCorregimiento(CatalogoDTO dto) {
        log.info("Creando corregimiento - nombre: {}", dto.getNombre());

        Corregimiento corregimiento = Corregimiento.builder()
                .nombre(dto.getNombre())
                .build();

        corregimiento = corregimientoRepository.save(corregimiento);
        log.info("Corregimiento creado con ID: {}", corregimiento.getId());

        return new ItemSimpleDTO(corregimiento.getId(), corregimiento.getNombre());
    }

    /**
     * Actualizar corregimiento
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public ItemSimpleDTO actualizarCorregimiento(Long id, CatalogoDTO dto) {
        log.info("Actualizando corregimiento ID: {}", id);

        Corregimiento corregimiento = corregimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Corregimiento no encontrado con ID: " + id));

        corregimiento.setNombre(dto.getNombre());
        corregimiento = corregimientoRepository.save(corregimiento);

        log.info("Corregimiento {} actualizado", id);

        return new ItemSimpleDTO(corregimiento.getId(), corregimiento.getNombre());
    }

    /**
     * Eliminar corregimiento
     */
    @PreAuthorize("hasAnyRole('DIRECTORA','ADMIN')")
    @Transactional
    public void eliminarCorregimiento(Long id) {
        log.info("Eliminando corregimiento ID: {}", id);

        if (!corregimientoRepository.existsById(id)) {
            throw new EntityNotFoundException("Corregimiento no encontrado con ID: " + id);
        }

        corregimientoRepository.deleteById(id);
        log.info("Corregimiento {} eliminado", id);
    }
}
