package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.DespachoComisarioService;
import com.neiva.querillas.web.dto.DespachoComisarioCreateDTO;
import com.neiva.querillas.web.dto.DespachoComisarioResponse;
import com.neiva.querillas.web.dto.DespachoComisarioUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/despachos-comisorios")
@RequiredArgsConstructor
@Slf4j
public class DespachoComisarioController {

    private final DespachoComisarioService despachoService;

    /**
     * Crear un nuevo despacho comisorio
     */
    @PostMapping
    public ResponseEntity<DespachoComisarioResponse> crear(@Valid @RequestBody DespachoComisarioCreateDTO dto) {
        log.info("POST /api/despachos-comisorios - radicado: {}", dto.getRadicadoProceso());
        DespachoComisarioResponse response = despachoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar un despacho comisorio existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<DespachoComisarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DespachoComisarioUpdateDTO dto) {
        log.info("PUT /api/despachos-comisorios/{}", id);
        DespachoComisarioResponse response = despachoService.actualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener un despacho comisorio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DespachoComisarioResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/despachos-comisorios/{}", id);
        DespachoComisarioResponse response = despachoService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los despachos comisorios con paginación
     */
    @GetMapping
    public ResponseEntity<Page<DespachoComisarioResponse>> listar(
            @PageableDefault(size = 20, sort = "fechaRecibido") Pageable pageable) {
        log.info("GET /api/despachos-comisorios - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<DespachoComisarioResponse> response = despachoService.listar(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar despachos comisorios por corregimiento
     */
    @GetMapping("/corregimiento/{corregimientoId}")
    public ResponseEntity<Page<DespachoComisarioResponse>> listarPorCorregimiento(
            @PathVariable Long corregimientoId,
            @PageableDefault(size = 20, sort = "fechaRecibido") Pageable pageable) {
        log.info("GET /api/despachos-comisorios/corregimiento/{}", corregimientoId);
        Page<DespachoComisarioResponse> response = despachoService.listarPorCorregimiento(corregimientoId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar despachos pendientes de devolución
     */
    @GetMapping("/pendientes-devolucion")
    public ResponseEntity<Page<DespachoComisarioResponse>> listarPendientesDevolucion(
            @PageableDefault(size = 20, sort = "fechaRecibido") Pageable pageable) {
        log.info("GET /api/despachos-comisorios/pendientes-devolucion");
        Page<DespachoComisarioResponse> response = despachoService.listarPendientesDevolucion(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Buscar despachos por entidad procedente
     */
    @GetMapping("/buscar-por-entidad")
    public ResponseEntity<Page<DespachoComisarioResponse>> buscarPorEntidad(
            @RequestParam String entidad,
            @PageableDefault(size = 20, sort = "fechaRecibido") Pageable pageable) {
        log.info("GET /api/despachos-comisorios/buscar-por-entidad?entidad={}", entidad);
        Page<DespachoComisarioResponse> response = despachoService.buscarPorEntidad(entidad, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener despachos por rango de fechas
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<DespachoComisarioResponse>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        log.info("GET /api/despachos-comisorios/rango-fechas?desde={}&hasta={}", desde, hasta);
        List<DespachoComisarioResponse> response = despachoService.listarPorRangoFechas(desde, hasta);
        return ResponseEntity.ok(response);
    }

    /**
     * Registrar devolución de un despacho
     */
    @PutMapping("/{id}/devolucion")
    public ResponseEntity<DespachoComisarioResponse> registrarDevolucion(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDevolucion) {
        log.info("PUT /api/despachos-comisorios/{}/devolucion?fecha={}", id, fechaDevolucion);
        DespachoComisarioResponse response = despachoService.registrarDevolucion(id, fechaDevolucion);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un despacho comisorio
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/despachos-comisorios/{}", id);
        despachoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
