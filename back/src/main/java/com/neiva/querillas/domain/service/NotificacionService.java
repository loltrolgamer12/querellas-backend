package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Notificacion;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.TipoNotificacion;
import com.neiva.querillas.domain.repo.NotificacionRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.ListaNotificacionesResponse;
import com.neiva.querillas.web.dto.NotificacionResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Listar notificaciones del usuario actual con filtro opcional por leída
     */
    @Transactional(readOnly = true)
    public ListaNotificacionesResponse listar(Boolean leida, int page, int size) {
        Long usuarioId = obtenerUsuarioActualId();
        log.info("Listando notificaciones - usuarioId: {}, leida: {}, page: {}, size: {}", usuarioId, leida, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Notificacion> notificacionesPage = notificacionRepository.findByUsuarioIdAndLeida(usuarioId, leida, pageable);
        long noLeidas = notificacionRepository.countNoLeidasByUsuarioId(usuarioId);

        return ListaNotificacionesResponse.builder()
                .items(notificacionesPage.getContent().stream()
                        .map(this::convertirAResponse)
                        .toList())
                .total(notificacionesPage.getTotalElements())
                .noLeidas(noLeidas)
                .build();
    }

    /**
     * Marcar notificación como leída
     */
    @Transactional
    public void marcarComoLeida(Long id) {
        Long usuarioId = obtenerUsuarioActualId();
        log.info("Marcando notificación {} como leída por usuario {}", id, usuarioId);

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + id));

        // Validar que la notificación pertenezca al usuario actual
        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permiso para marcar esta notificación");
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    @Transactional
    public Map<String, Integer> marcarTodasComoLeidas() {
        Long usuarioId = obtenerUsuarioActualId();
        log.info("Marcando todas las notificaciones como leídas para usuario {}", usuarioId);

        int marcadas = notificacionRepository.marcarTodasComoLeidas(usuarioId);

        Map<String, Integer> result = new HashMap<>();
        result.put("marcadas", marcadas);
        return result;
    }

    /**
     * Eliminar notificación
     */
    @Transactional
    public void eliminar(Long id) {
        Long usuarioId = obtenerUsuarioActualId();
        log.info("Eliminando notificación {} por usuario {}", id, usuarioId);

        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificación no encontrada con ID: " + id));

        // Validar que la notificación pertenezca al usuario actual
        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tiene permiso para eliminar esta notificación");
        }

        notificacionRepository.delete(notificacion);
    }

    /**
     * Crear notificación de asignación de querella
     * Este método será llamado desde QuerellaService cuando se asigna una inspección
     */
    @Transactional
    public void crearNotificacionAsignacion(Querella querella, Long usuarioAsignadoId) {
        log.info("Creando notificación de asignación para querella {} y usuario {}", querella.getId(), usuarioAsignadoId);

        Usuario usuario = usuarioRepository.findById(usuarioAsignadoId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioAsignadoId));

        Notificacion notificacion = Notificacion.builder()
                .titulo("Caso #" + querella.getRadicadoInterno() + " asignado")
                .mensaje("Se le ha asignado un nuevo caso de inspección")
                .tipo(TipoNotificacion.ASIGNACION)
                .leida(false)
                .querella(querella)
                .usuario(usuario)
                .build();

        notificacionRepository.save(notificacion);
        log.info("Notificación de asignación creada con ID: {}", notificacion.getId());
    }

    /**
     * Crear notificación de cambio de estado
     * Este método será llamado desde QuerellaService cuando cambia el estado
     */
    @Transactional
    public void crearNotificacionCambioEstado(Querella querella, String nuevoEstado, Long usuarioNotificarId) {
        log.info("Creando notificación de cambio de estado para querella {}, nuevo estado: {}, usuario: {}",
                querella.getId(), nuevoEstado, usuarioNotificarId);

        Usuario usuario = usuarioRepository.findById(usuarioNotificarId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + usuarioNotificarId));

        Notificacion notificacion = Notificacion.builder()
                .titulo("Cambio de estado en caso #" + querella.getRadicadoInterno())
                .mensaje("El caso ha cambiado a estado: " + nuevoEstado)
                .tipo(TipoNotificacion.CAMBIO_ESTADO)
                .leida(false)
                .querella(querella)
                .usuario(usuario)
                .build();

        notificacionRepository.save(notificacion);
        log.info("Notificación de cambio de estado creada con ID: {}", notificacion.getId());
    }

    /**
     * Obtener el ID del usuario actualmente autenticado
     */
    private Long obtenerUsuarioActualId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        // Por ahora retornamos un ID hardcodeado ya que el sistema de autenticación
        // usa InMemoryUserDetailsService. En producción esto debería obtener el ID real del usuario
        // desde el token JWT o la sesión
        String username = authentication.getName();

        // Mapeo temporal de usuarios en memoria a IDs
        // En producción, esto debe resolverse desde la BD
        return switch (username) {
            case "directora" -> 1L;
            case "auxiliar" -> 2L;
            case "inspector" -> 3L;
            default -> 1L;
        };
    }

    /**
     * Convertir entidad Notificacion a DTO de respuesta
     */
    private NotificacionResponse convertirAResponse(Notificacion notificacion) {
        return NotificacionResponse.builder()
                .id(notificacion.getId())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .tipo(notificacion.getTipo())
                .leida(notificacion.getLeida())
                .querellaId(notificacion.getQuerella() != null ? notificacion.getQuerella().getId() : null)
                .creadoEn(notificacion.getCreadoEn())
                .usuarioId(notificacion.getUsuario().getId())
                .build();
    }
}
