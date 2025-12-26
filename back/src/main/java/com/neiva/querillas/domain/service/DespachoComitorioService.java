package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.repo.DespachoComitorioRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DespachoComitorioService {

    private final DespachoComitorioRepository despachoRepo;
    private final UsuarioRepository usuarioRepo;

    /**
     * Crear un nuevo despacho comisorio
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public DespachoComitorioResponse crear(DespachoComitorioCreateDTO dto) {
        log.info("Creando despacho comisorio - número: {}", dto.getNumeroDespacho());

        // Validar que no exista un despacho con el mismo número
        if (despachoRepo.existsByNumeroDespacho(dto.getNumeroDespacho())) {
            throw new IllegalArgumentException("Ya existe un despacho con el número: " + dto.getNumeroDespacho());
        }

        // Obtener inspector si fue asignado
        Usuario inspector = null;
        if (dto.getInspectorAsignadoId() != null) {
            inspector = usuarioRepo.findById(dto.getInspectorAsignadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Inspector no encontrado"));
        }

        // Obtener usuario que asigna
        Usuario asignadoPor = null;
        if (dto.getAsignadoPorId() != null) {
            asignadoPor = usuarioRepo.findById(dto.getAsignadoPorId()).orElse(null);
        }

        DespachoComisorio despacho = DespachoComisorio.builder()
                .fechaRecibido(dto.getFechaRecibido())
                .radicadoProceso(dto.getRadicadoProceso())
                .numeroDespacho(dto.getNumeroDespacho())
                .entidadProcedente(dto.getEntidadProcedente())
                .asunto(dto.getAsunto())
                .demandanteApoderado(dto.getDemandanteApoderado())
                .demandadoApoderado(dto.getDemandadoApoderado())
                .inspectorAsignado(inspector)
                .asignadoPor(asignadoPor)
                .fechaDevolucion(dto.getFechaDevolucion())
                .observaciones(dto.getObservaciones())
                .build();

        despacho = despachoRepo.save(despacho);
        log.info("Despacho comisorio creado con ID: {}", despacho.getId());

        return toResponse(despacho);
    }

    /**
     * Obtener despacho por ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public DespachoComitorioResponse obtenerPorId(Long id) {
        log.info("Obteniendo despacho comisorio ID: {}", id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho no encontrado con ID: " + id));

        return toResponse(despacho);
    }

    /**
     * Listar todos los despachos con paginación
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public Page<DespachoComitorioResponse> listar(int page, int size, String sortBy, String direction) {
        log.info("Listando despachos - page: {}, size: {}", page, size);

        Sort.Direction dir = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy != null ? sortBy : "fechaRecibido"));

        Page<DespachoComisorio> despachos = despachoRepo.findAll(pageable);

        return despachos.map(this::toResponse);
    }

    /**
     * Listar despachos pendientes (sin devolver)
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public List<DespachoComitorioResponse> listarPendientes() {
        log.info("Listando despachos pendientes");

        List<DespachoComisorio> despachos = despachoRepo.findPendientes();

        return despachos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Listar despachos devueltos
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public List<DespachoComitorioResponse> listarDevueltos() {
        log.info("Listando despachos devueltos");

        List<DespachoComisorio> despachos = despachoRepo.findDevueltos();

        return despachos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Listar por inspector
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public List<DespachoComitorioResponse> listarPorInspector(Long inspectorId) {
        log.info("Listando despachos del inspector ID: {}", inspectorId);

        List<DespachoComisorio> despachos = despachoRepo.findByInspectorAsignadoId(inspectorId);

        return despachos.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Actualizar despacho
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public DespachoComitorioResponse actualizar(Long id, DespachoComitorioUpdateDTO dto) {
        log.info("Actualizando despacho ID: {}", id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho no encontrado con ID: " + id));

        // Validar número de despacho único si cambió
        if (!despacho.getNumeroDespacho().equals(dto.getNumeroDespacho())) {
            if (despachoRepo.existsByNumeroDespacho(dto.getNumeroDespacho())) {
                throw new IllegalArgumentException("Ya existe un despacho con el número: " + dto.getNumeroDespacho());
            }
        }

        // Obtener inspector si fue asignado
        if (dto.getInspectorAsignadoId() != null) {
            Usuario inspector = usuarioRepo.findById(dto.getInspectorAsignadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Inspector no encontrado"));
            despacho.setInspectorAsignado(inspector);
        } else {
            despacho.setInspectorAsignado(null);
        }

        // Actualizar campos
        despacho.setFechaRecibido(dto.getFechaRecibido());
        despacho.setRadicadoProceso(dto.getRadicadoProceso());
        despacho.setNumeroDespacho(dto.getNumeroDespacho());
        despacho.setEntidadProcedente(dto.getEntidadProcedente());
        despacho.setAsunto(dto.getAsunto());
        despacho.setDemandanteApoderado(dto.getDemandanteApoderado());
        despacho.setDemandadoApoderado(dto.getDemandadoApoderado());
        despacho.setFechaDevolucion(dto.getFechaDevolucion());
        despacho.setObservaciones(dto.getObservaciones());

        despacho = despachoRepo.save(despacho);
        log.info("Despacho {} actualizado", id);

        return toResponse(despacho);
    }

    /**
     * Asignar inspector a despacho
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public DespachoComitorioResponse asignarInspector(Long id, Long inspectorId, Long asignadoPorId) {
        log.info("Asignando inspector {} al despacho {}", inspectorId, id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho no encontrado con ID: " + id));

        Usuario inspector = usuarioRepo.findById(inspectorId)
                .orElseThrow(() -> new EntityNotFoundException("Inspector no encontrado"));

        Usuario asignadoPor = null;
        if (asignadoPorId != null) {
            asignadoPor = usuarioRepo.findById(asignadoPorId).orElse(null);
        }

        despacho.setInspectorAsignado(inspector);
        despacho.setAsignadoPor(asignadoPor);

        despacho = despachoRepo.save(despacho);
        log.info("Inspector asignado al despacho {}", id);

        return toResponse(despacho);
    }

    /**
     * Marcar como devuelto
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public DespachoComitorioResponse marcarComoDevuelto(Long id, OffsetDateTime fechaDevolucion) {
        log.info("Marcando despacho {} como devuelto", id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho no encontrado con ID: " + id));

        despacho.setFechaDevolucion(fechaDevolucion != null ? fechaDevolucion : OffsetDateTime.now());

        despacho = despachoRepo.save(despacho);
        log.info("Despacho {} marcado como devuelto", id);

        return toResponse(despacho);
    }

    /**
     * Eliminar despacho
     */
    @Transactional
    @PreAuthorize("hasRole('DIRECTOR')")
    public void eliminar(Long id) {
        log.info("Eliminando despacho ID: {}", id);

        if (!despachoRepo.existsById(id)) {
            throw new EntityNotFoundException("Despacho no encontrado con ID: " + id);
        }

        despachoRepo.deleteById(id);
        log.info("Despacho {} eliminado", id);
    }

    /**
     * Generar reporte por rango de fechas
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public List<DespachoComitorioReporteDTO> generarReporte(OffsetDateTime desde, OffsetDateTime hasta) {
        log.info("Generando reporte de despachos - desde: {}, hasta: {}", desde, hasta);

        List<DespachoComisorio> despachos = despachoRepo.findByFechaRecibidoBetween(desde, hasta);

        int item = 1;
        return despachos.stream()
                .map(d -> toReporteDTO(d, item))
                .toList();
    }

    // ======================
    // HELPERS
    // ======================

    private DespachoComitorioResponse toResponse(DespachoComisorio despacho) {
        String estado = despacho.getFechaDevolucion() != null ? "DEVUELTO" : "PENDIENTE";

        return DespachoComitorioResponse.builder()
                .id(despacho.getId())
                .fechaRecibido(despacho.getFechaRecibido())
                .radicadoProceso(despacho.getRadicadoProceso())
                .numeroDespacho(despacho.getNumeroDespacho())
                .entidadProcedente(despacho.getEntidadProcedente())
                .asunto(despacho.getAsunto())
                .demandanteApoderado(despacho.getDemandanteApoderado())
                .demandadoApoderado(despacho.getDemandadoApoderado())
                .inspectorAsignadoId(despacho.getInspectorAsignado() != null ? despacho.getInspectorAsignado().getId() : null)
                .inspectorAsignadoNombre(despacho.getInspectorAsignado() != null ? despacho.getInspectorAsignado().getNombre() : null)
                .inspectorAsignadoZona(despacho.getInspectorAsignado() != null && despacho.getInspectorAsignado().getZona() != null ?
                        despacho.getInspectorAsignado().getZona().name() : null)
                .asignadoPorId(despacho.getAsignadoPor() != null ? despacho.getAsignadoPor().getId() : null)
                .asignadoPorNombre(despacho.getAsignadoPor() != null ? despacho.getAsignadoPor().getNombre() : null)
                .fechaDevolucion(despacho.getFechaDevolucion())
                .observaciones(despacho.getObservaciones())
                .estado(estado)
                .creadoEn(despacho.getCreadoEn())
                .actualizadoEn(despacho.getActualizadoEn())
                .build();
    }

    private DespachoComitorioReporteDTO toReporteDTO(DespachoComisorio d, int item) {
        String inspectorInfo = "";
        if (d.getInspectorAsignado() != null) {
            inspectorInfo = d.getInspectorAsignado().getNombre();
            if (d.getInspectorAsignado().getZona() != null) {
                inspectorInfo += " (" + d.getInspectorAsignado().getZona().name() + ")";
            }
        }

        return DespachoComitorioReporteDTO.builder()
                .item(item)
                .fechaRecibido(d.getFechaRecibido())
                .radicadoProceso(d.getRadicadoProceso())
                .numeroDespacho(d.getNumeroDespacho())
                .entidadProcedente(d.getEntidadProcedente())
                .asunto(d.getAsunto())
                .demandanteApoderado(d.getDemandanteApoderado())
                .demandadoApoderado(d.getDemandadoApoderado())
                .inspectorAsignado(inspectorInfo)
                .fechaDevolucion(d.getFechaDevolucion())
                .build();
    }
}
