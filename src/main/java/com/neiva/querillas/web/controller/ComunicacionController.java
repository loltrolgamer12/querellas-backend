package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.ComunicacionService;
import com.neiva.querillas.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/querellas/{querellaId}/comunicaciones")
@RequiredArgsConstructor
@Slf4j
public class ComunicacionController {

    private final ComunicacionService comunicacionService;

    /**
     * GET /api/querellas/{id}/comunicaciones
     * Listar comunicaciones de una querella
     */
    @GetMapping
    public ResponseEntity<List<ComunicacionResponse>> listar(@PathVariable Long querellaId) {
        log.info("GET /api/querellas/{}/comunicaciones", querellaId);
        List<ComunicacionResponse> response = comunicacionService.listar(querellaId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/querellas/{id}/comunicaciones
     * Crear comunicación
     */
    @PostMapping
    public ResponseEntity<ComunicacionResponse> crear(
            @PathVariable Long querellaId,
            @Valid @RequestBody ComunicacionCreateDTO dto) {
        log.info("POST /api/querellas/{}/comunicaciones - tipo: {}", querellaId, dto.getTipo());
        ComunicacionResponse response = comunicacionService.crear(querellaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/querellas/{querellaId}/comunicaciones/{comunicacionId}
     * Actualizar comunicación
     */
    @PutMapping("/{comunicacionId}")
    public ResponseEntity<ComunicacionResponse> actualizar(
            @PathVariable Long querellaId,
            @PathVariable Long comunicacionId,
            @Valid @RequestBody ComunicacionUpdateDTO dto) {
        log.info("PUT /api/querellas/{}/comunicaciones/{}", querellaId, comunicacionId);
        ComunicacionResponse response = comunicacionService.actualizar(querellaId, comunicacionId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/querellas/{querellaId}/comunicaciones/{comunicacionId}/estado
     * Cambiar estado de comunicación
     */
    @PutMapping("/{comunicacionId}/estado")
    public ResponseEntity<ComunicacionResponse> cambiarEstado(
            @PathVariable Long querellaId,
            @PathVariable Long comunicacionId,
            @Valid @RequestBody CambioEstadoComunicacionDTO dto) {
        log.info("PUT /api/querellas/{}/comunicaciones/{}/estado - nuevo estado: {}",
                querellaId, comunicacionId, dto.getNuevoEstado());
        ComunicacionResponse response = comunicacionService.cambiarEstado(querellaId, comunicacionId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/querellas/{querellaId}/comunicaciones/{comunicacionId}
     * Eliminar comunicación
     */
    @DeleteMapping("/{comunicacionId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long querellaId,
            @PathVariable Long comunicacionId) {
        log.info("DELETE /api/querellas/{}/comunicaciones/{}", querellaId, comunicacionId);
        comunicacionService.eliminar(querellaId, comunicacionId);
        return ResponseEntity.noContent().build();
    }
}
