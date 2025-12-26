package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Comunicacion;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.repo.ComunicacionRepository;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComunicacionService {

    private final ComunicacionRepository comunicacionRepository;
    private final QuerellaRepository querellaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Listar comunicaciones de una querella
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional(readOnly = true)
    public List<ComunicacionResponse> listar(Long querellaId) {
        log.info("Listando comunicaciones de querella {}", querellaId);

        // Validar que la querella existe
        if (!querellaRepository.existsById(querellaId)) {
            throw new EntityNotFoundException("Querella no encontrada con ID: " + querellaId);
        }

        List<Comunicacion> comunicaciones = comunicacionRepository.findByQuerellaIdOrderByCreadoEnDesc(querellaId);

        return comunicaciones.stream()
                .map(this::convertirAResponse)
                .toList();
    }

    /**
     * Crear comunicación
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public ComunicacionResponse crear(Long querellaId, ComunicacionCreateDTO dto) {
        log.info("Creando comunicación para querella {} - tipo: {}", querellaId, dto.getTipo());

        // Validar que la querella existe
        Querella querella = querellaRepository.findById(querellaId)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada con ID: " + querellaId));

        // Obtener usuario actual
        Long usuarioId = obtenerUsuarioActualId();
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        // Crear comunicación
        Comunicacion comunicacion = Comunicacion.builder()
                .querella(querella)
                .tipo(dto.getTipo())
                .numeroRadicado(dto.getNumeroRadicado())
                .asunto(dto.getAsunto())
                .contenido(dto.getContenido())
                .fechaEnvio(dto.getFechaEnvio())
                .destinatario(dto.getDestinatario())
                .estado(dto.getEstado())
                .creadoPor(usuario)
                .build();

        comunicacion = comunicacionRepository.save(comunicacion);
        log.info("Comunicación creada con ID: {}", comunicacion.getId());

        return convertirAResponse(comunicacion);
    }

    /**
     * Actualizar comunicación
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public ComunicacionResponse actualizar(Long querellaId, Long comunicacionId, ComunicacionUpdateDTO dto) {
        log.info("Actualizando comunicación {} de querella {}", comunicacionId, querellaId);

        Comunicacion comunicacion = comunicacionRepository.findById(comunicacionId)
                .orElseThrow(() -> new EntityNotFoundException("Comunicación no encontrada con ID: " + comunicacionId));

        // Validar que la comunicación pertenece a la querella
        if (!comunicacion.getQuerella().getId().equals(querellaId)) {
            throw new IllegalArgumentException("La comunicación no pertenece a la querella especificada");
        }

        // Actualizar campos
        comunicacion.setTipo(dto.getTipo());
        comunicacion.setNumeroRadicado(dto.getNumeroRadicado());
        comunicacion.setAsunto(dto.getAsunto());
        comunicacion.setContenido(dto.getContenido());
        comunicacion.setFechaEnvio(dto.getFechaEnvio());
        comunicacion.setDestinatario(dto.getDestinatario());
        comunicacion.setEstado(dto.getEstado());

        comunicacion = comunicacionRepository.save(comunicacion);
        log.info("Comunicación {} actualizada", comunicacionId);

        return convertirAResponse(comunicacion);
    }

    /**
     * Cambiar estado de comunicación
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public ComunicacionResponse cambiarEstado(Long querellaId, Long comunicacionId, CambioEstadoComunicacionDTO dto) {
        log.info("Cambiando estado de comunicación {} a {}", comunicacionId, dto.getNuevoEstado());

        Comunicacion comunicacion = comunicacionRepository.findById(comunicacionId)
                .orElseThrow(() -> new EntityNotFoundException("Comunicación no encontrada con ID: " + comunicacionId));

        // Validar que la comunicación pertenece a la querella
        if (!comunicacion.getQuerella().getId().equals(querellaId)) {
            throw new IllegalArgumentException("La comunicación no pertenece a la querella especificada");
        }

        comunicacion.setEstado(dto.getNuevoEstado());
        comunicacion = comunicacionRepository.save(comunicacion);

        log.info("Estado de comunicación {} cambiado a: {}", comunicacionId, dto.getNuevoEstado());

        return convertirAResponse(comunicacion);
    }

    /**
     * Eliminar comunicación
     */
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    @Transactional
    public void eliminar(Long querellaId, Long comunicacionId) {
        log.info("Eliminando comunicación {} de querella {}", comunicacionId, querellaId);

        Comunicacion comunicacion = comunicacionRepository.findById(comunicacionId)
                .orElseThrow(() -> new EntityNotFoundException("Comunicación no encontrada con ID: " + comunicacionId));

        // Validar que la comunicación pertenece a la querella
        if (!comunicacion.getQuerella().getId().equals(querellaId)) {
            throw new IllegalArgumentException("La comunicación no pertenece a la querella especificada");
        }

        comunicacionRepository.delete(comunicacion);
        log.info("Comunicación {} eliminada", comunicacionId);
    }

    /**
     * Obtener el ID del usuario actualmente autenticado
     */
    private Long obtenerUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        String username = authentication.getName();

        // Mapeo temporal de usuarios en memoria a IDs
        return switch (username) {
            case "directora" -> 1L;
            case "auxiliar" -> 2L;
            case "inspector" -> 3L;
            default -> 1L;
        };
    }

    /**
     * Convertir entidad Comunicacion a DTO de respuesta
     */
    private ComunicacionResponse convertirAResponse(Comunicacion comunicacion) {
        return ComunicacionResponse.builder()
                .id(comunicacion.getId())
                .tipo(comunicacion.getTipo())
                .numeroRadicado(comunicacion.getNumeroRadicado())
                .asunto(comunicacion.getAsunto())
                .contenido(comunicacion.getContenido())
                .fechaEnvio(comunicacion.getFechaEnvio())
                .destinatario(comunicacion.getDestinatario())
                .estado(comunicacion.getEstado())
                .creadoPor(ComunicacionResponse.CreadoPorDTO.builder()
                        .id(comunicacion.getCreadoPor().getId())
                        .nombre(comunicacion.getCreadoPor().getNombre())
                        .build())
                .creadoEn(comunicacion.getCreadoEn())
                .build();
    }
}
