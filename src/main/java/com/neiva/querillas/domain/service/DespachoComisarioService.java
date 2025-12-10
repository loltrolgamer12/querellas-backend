package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.Corregimiento;
import com.neiva.querillas.domain.entity.DespachoComisorio;
import com.neiva.querillas.domain.repo.CorregimientoRepository;
import com.neiva.querillas.domain.repo.DespachoComisarioRepository;
import com.neiva.querillas.web.dto.DespachoComisarioCreateDTO;
import com.neiva.querillas.web.dto.DespachoComisarioResponse;
import com.neiva.querillas.web.dto.DespachoComisarioUpdateDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DespachoComisarioService {

    private final DespachoComisarioRepository despachoRepo;
    private final CorregimientoRepository corregimientoRepo;

    /**
     * Crear un nuevo despacho comisorio
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public DespachoComisarioResponse crear(DespachoComisarioCreateDTO dto) {
        log.info("Creando despacho comisorio - radicado: {}", dto.getRadicadoProceso());

        Corregimiento corregimiento = null;
        if (dto.getCorregimientoId() != null) {
            corregimiento = corregimientoRepo.findById(dto.getCorregimientoId())
                    .orElseThrow(() -> new EntityNotFoundException("Corregimiento no encontrado con ID: " + dto.getCorregimientoId()));
        }

        DespachoComisorio despacho = DespachoComisorio.builder()
                .fechaRecibido(dto.getFechaRecibido())
                .radicadoProceso(dto.getRadicadoProceso())
                .numeroDespacho(dto.getNumeroDespacho())
                .entidadProcedente(dto.getEntidadProcedente())
                .asunto(dto.getAsunto())
                .demandanteApoderado(dto.getDemandanteApoderado())
                .corregimientoAsignado(corregimiento)
                .fechaDevolucion(dto.getFechaDevolucion())
                .build();

        despacho = despachoRepo.save(despacho);
        log.info("Despacho comisorio creado con ID: {}", despacho.getId());

        return toResponse(despacho);
    }

    /**
     * Actualizar un despacho comisorio existente
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public DespachoComisarioResponse actualizar(Long id, DespachoComisarioUpdateDTO dto) {
        log.info("Actualizando despacho comisorio ID: {}", id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho comisorio no encontrado con ID: " + id));

        // Actualizar campos básicos
        if (dto.getFechaRecibido() != null) {
            despacho.setFechaRecibido(dto.getFechaRecibido());
        }
        if (dto.getRadicadoProceso() != null) {
            despacho.setRadicadoProceso(dto.getRadicadoProceso());
        }
        if (dto.getNumeroDespacho() != null) {
            despacho.setNumeroDespacho(dto.getNumeroDespacho());
        }
        if (dto.getEntidadProcedente() != null) {
            despacho.setEntidadProcedente(dto.getEntidadProcedente());
        }
        if (dto.getAsunto() != null) {
            despacho.setAsunto(dto.getAsunto());
        }
        if (dto.getDemandanteApoderado() != null) {
            despacho.setDemandanteApoderado(dto.getDemandanteApoderado());
        }
        if (dto.getFechaDevolucion() != null) {
            despacho.setFechaDevolucion(dto.getFechaDevolucion());
        }

        // Actualizar corregimiento si se proporciona
        if (dto.getCorregimientoId() != null) {
            Corregimiento corregimiento = corregimientoRepo.findById(dto.getCorregimientoId())
                    .orElseThrow(() -> new EntityNotFoundException("Corregimiento no encontrado con ID: " + dto.getCorregimientoId()));
            despacho.setCorregimientoAsignado(corregimiento);
        }

        despacho = despachoRepo.save(despacho);
        log.info("Despacho comisorio {} actualizado", id);

        return toResponse(despacho);
    }

    /**
     * Obtener un despacho comisorio por ID
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public DespachoComisarioResponse obtenerPorId(Long id) {
        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho comisorio no encontrado con ID: " + id));

        return toResponse(despacho);
    }

    /**
     * Listar todos los despachos comisorios con paginación
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public Page<DespachoComisarioResponse> listar(Pageable pageable) {
        return despachoRepo.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Listar despachos comisorios por corregimiento
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public Page<DespachoComisarioResponse> listarPorCorregimiento(Long corregimientoId, Pageable pageable) {
        return despachoRepo.findByCorregimientoAsignadoId(corregimientoId, pageable)
                .map(this::toResponse);
    }

    /**
     * Listar despachos pendientes de devolución
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public Page<DespachoComisarioResponse> listarPendientesDevolucion(Pageable pageable) {
        return despachoRepo.findPendientesDevolucion(pageable)
                .map(this::toResponse);
    }

    /**
     * Buscar despachos por entidad procedente
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public Page<DespachoComisarioResponse> buscarPorEntidad(String entidad, Pageable pageable) {
        return despachoRepo.findByEntidadProcedenteContainingIgnoreCase(entidad, pageable)
                .map(this::toResponse);
    }

    /**
     * Obtener despachos por rango de fechas
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public List<DespachoComisarioResponse> listarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        return despachoRepo.findByFechaRecibidoBetween(desde, hasta).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Registrar devolución de un despacho
     */
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public DespachoComisarioResponse registrarDevolucion(Long id, LocalDate fechaDevolucion) {
        log.info("Registrando devolución de despacho comisorio ID: {}", id);

        DespachoComisorio despacho = despachoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despacho comisorio no encontrado con ID: " + id));

        despacho.setFechaDevolucion(fechaDevolucion);
        despacho = despachoRepo.save(despacho);

        log.info("Devolución registrada para despacho {}", id);

        return toResponse(despacho);
    }

    /**
     * Eliminar un despacho comisorio
     */
    @Transactional
    @PreAuthorize("hasRole('DIRECTORA')")
    public void eliminar(Long id) {
        log.info("Eliminando despacho comisorio ID: {}", id);

        if (!despachoRepo.existsById(id)) {
            throw new EntityNotFoundException("Despacho comisorio no encontrado con ID: " + id);
        }

        despachoRepo.deleteById(id);
        log.info("Despacho comisorio {} eliminado", id);
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private DespachoComisarioResponse toResponse(DespachoComisorio despacho) {
        return DespachoComisarioResponse.builder()
                .id(despacho.getId())
                .fechaRecibido(despacho.getFechaRecibido())
                .radicadoProceso(despacho.getRadicadoProceso())
                .numeroDespacho(despacho.getNumeroDespacho())
                .entidadProcedente(despacho.getEntidadProcedente())
                .asunto(despacho.getAsunto())
                .demandanteApoderado(despacho.getDemandanteApoderado())
                .corregimientoId(despacho.getCorregimientoAsignado() != null ?
                        despacho.getCorregimientoAsignado().getId() : null)
                .corregimientoNombre(despacho.getCorregimientoAsignado() != null ?
                        despacho.getCorregimientoAsignado().getNombre() : null)
                .fechaDevolucion(despacho.getFechaDevolucion())
                .creadoEn(despacho.getCreadoEn())
                .actualizadoEn(despacho.getActualizadoEn())
                .build();
    }
}
