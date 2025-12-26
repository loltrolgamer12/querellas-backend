package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Comuna;
import com.neiva.querillas.domain.entity.Tema;
import com.neiva.querillas.domain.repo.ComunaRepository;
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

    private final TemaRepository temaRepository;
    private final ComunaRepository comunaRepository;

    // ==================== TEMAS ====================

    /**
     * Crear tema
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
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
    @PreAuthorize("hasAnyRole('DIRECTOR','ADMIN')")
    @Transactional
    public void eliminarComuna(Long id) {
        log.info("Eliminando comuna ID: {}", id);

        if (!comunaRepository.existsById(id)) {
            throw new EntityNotFoundException("Comuna no encontrada con ID: " + id);
        }

        comunaRepository.deleteById(id);
        log.info("Comuna {} eliminada", id);
    }
}
