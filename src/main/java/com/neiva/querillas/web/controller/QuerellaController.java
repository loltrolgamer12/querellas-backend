package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.AsignacionAutomaticaService;
import com.neiva.querillas.domain.service.QuerellaService;
import com.neiva.querillas.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // [CAMBIO] import para seguridad en métodos
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/querellas")
@RequiredArgsConstructor
public class QuerellaController {

    private final QuerellaService service;
    private final AsignacionAutomaticaService asignacionAutomaticaService;

    // 1. Crear querella
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR')") // [CAMBIO] Solo DIRECTORA y AUXILIAR pueden crear
    public ResponseEntity<QuerellaResponse> crear(@Valid @RequestBody QuerellaCreateDTO dto) {
        return ResponseEntity.ok(service.crear(dto));
    }

    // 2. Detalle por ID (lectura)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')") // [CAMBIO] Lectura para los 3 roles
    public ResponseEntity<QuerellaResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalle(id));
    }

    // 3. Bandeja con filtros + paginación + ordenamiento (lectura)
    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')") // [CAMBIO] Lectura para los 3 roles
    public ResponseEntity<PaginaQuerellaResponse> listarBandeja(
            @RequestParam(required = false) String qTexto,
            @RequestParam(required = false) String estadoNombre,
            @RequestParam(required = false) Long inspectorId,
            @RequestParam(required = false) String temaNombre,
            @RequestParam(required = false) Long comunaId,
            @RequestParam(required = false) OffsetDateTime desde,
            @RequestParam(required = false) OffsetDateTime hasta,
            @RequestParam(defaultValue = "0") int page, // página (0 = primera)
            @RequestParam(defaultValue = "10") int size, // filas por página
            @RequestParam(required = false, name = "sort") String sortParam // ej: "creadoEn,DESC"
    ) {
        PaginaQuerellaResponse resp = service.listarBandeja(
                qTexto,
                estadoNombre,
                inspectorId,
                temaNombre,
                comunaId,
                desde,
                hasta,
                page,
                size,
                sortParam);
        return ResponseEntity.ok(resp);
    }

    // 4. Asignar / Reasignar inspector
    @PutMapping("/{id}/inspector")
    @PreAuthorize("hasAnyRole('DIRECTORA','INSPECTOR')") // [CAMBIO] DIRECTORA o INSPECTOR
    public ResponseEntity<QuerellaResponse> asignarInspector(
            @PathVariable Long id,
            @Valid @RequestBody AsignarInspectorDTO dto,
            @RequestParam(required = false) Long asignadoPorId) {
        return ResponseEntity.ok(service.asignarInspector(id, dto, asignadoPorId));
    }

    // 5. Cambiar estado
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('DIRECTORA','INSPECTOR')") // [CAMBIO] DIRECTORA o INSPECTOR
    public ResponseEntity<QuerellaResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto));
    }

    // 6. Historial de estados (lectura)
    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')") // [CAMBIO] Lectura para los 3 roles
    public ResponseEntity<List<HistorialEstadoDTO>> historial(@PathVariable Long id) {
        return ResponseEntity.ok(service.historialEstados(id));
    }

    // 7. Posibles duplicados para una querella
    @GetMapping("/{id}/posibles-duplicados")
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public ResponseEntity<List<QuerellaResponse>> posiblesDuplicados(@PathVariable Long id) {
        return ResponseEntity.ok(service.posiblesDuplicados(id));
    }

    // 8. Asignación automática de querellas (Round-Robin)
    @PostMapping("/asignar-automatico")
    @PreAuthorize("hasRole('DIRECTORA')")
    public ResponseEntity<AsignacionAutomaticaResponse> asignarAutomatico(
            @Valid @RequestBody AsignacionAutomaticaRequest request) {
        return ResponseEntity.ok(asignacionAutomaticaService.asignarAutomaticamente(request));
    }

}
