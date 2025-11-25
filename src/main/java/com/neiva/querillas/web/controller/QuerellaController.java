package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.QuerellaService;
import com.neiva.querillas.web.dto.AsignarInspeccionDTO;
import com.neiva.querillas.web.dto.CambioEstadoDTO;
import com.neiva.querillas.web.dto.HistorialEstadoDTO;
import com.neiva.querillas.web.dto.PaginaQuerellaResponse;
import com.neiva.querillas.web.dto.QuerellaCreateDTO;
import com.neiva.querillas.web.dto.QuerellaResponse;
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
            @RequestParam(required = false) Long inspeccionId,
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
                inspeccionId,
                comunaId,
                desde,
                hasta,
                page,
                size,
                sortParam);
        return ResponseEntity.ok(resp);
    }

    // 4. Asignar / Reasignar inspección
    @PutMapping("/{id}/inspeccion")
    @PreAuthorize("hasAnyRole('DIRECTORA','INSPECTOR')") // [CAMBIO] DIRECTORA o INSPECTOR
    public ResponseEntity<QuerellaResponse> asignarInspeccion(
            @PathVariable Long id,
            @Valid @RequestBody AsignarInspeccionDTO dto) {
        return ResponseEntity.ok(service.asignarInspeccion(id, dto));
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

}

// package com.neiva.querillas.web.controller;

// import com.neiva.querillas.domain.service.QuerellaService;
// import com.neiva.querillas.web.dto.CambioEstadoDTO;
// import com.neiva.querillas.web.dto.QuerellaCreateDTO;
// import com.neiva.querillas.web.dto.QuerellaResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.format.annotation.DateTimeFormat;
// import java.time.OffsetDateTime;
// import java.util.List;

// @RestController
// @RequestMapping("/api/querellas")
// @RequiredArgsConstructor
// public class QuerellaController {

// private final QuerellaService service;

// // 1. crear querella
// @PostMapping
// public ResponseEntity<QuerellaResponse> crear(@RequestBody QuerellaCreateDTO
// body) {
// QuerellaResponse resp = service.crear(body);
// return ResponseEntity.ok(resp);
// }

// // 2. asignar / cambiar inspección (genera id_local por trigger)
// @PutMapping("/{id}/inspeccion/{inspeccionId}")
// public ResponseEntity<QuerellaResponse> asignarInspeccion(
// @PathVariable Long id,
// @PathVariable Long inspeccionId
// ) {
// QuerellaResponse resp = service.asignarInspeccion(id, inspeccionId);
// return ResponseEntity.ok(resp);
// }

// // 3. forzar cambio de estado (inserta en historial_estado)
// @PostMapping("/{id}/estado")
// public ResponseEntity<QuerellaResponse> cambiarEstado(
// @PathVariable Long id,
// @RequestBody CambioEstadoDTO body
// ) {
// QuerellaResponse resp = service.cambiarEstado(
// id,
// body.getNuevoEstado(),
// body.getMotivo(),
// body.getUsuarioId()
// );
// return ResponseEntity.ok(resp);
// }

// // 4. detalle
// @GetMapping("/{id}")
// public ResponseEntity<QuerellaResponse> detalle(@PathVariable Long id) {
// QuerellaResponse resp = service.detalle(id);
// return ResponseEntity.ok(resp);
// }

// @GetMapping
// public ResponseEntity<List<QuerellaResponse>> listarBandeja(
// @RequestParam(required = false) String q,
// @RequestParam(required = false) String estado,
// @RequestParam(required = false) Long inspeccionId,
// @RequestParam(required = false) Long comunaId,
// @RequestParam(required = false) @DateTimeFormat(iso =
// DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
// @RequestParam(required = false) @DateTimeFormat(iso =
// DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta
// ) {
// List<QuerellaResponse> data = service.listarBandeja(
// q,
// estado,
// inspeccionId,
// comunaId,
// desde,
// hasta
// );
// return ResponseEntity.ok(data);
// }

// }

// package com.neiva.querillas.web.controller;

// import com.neiva.querillas.domain.entity.Querella;
// import com.neiva.querillas.domain.service.QuerellaService;
// import com.neiva.querillas.web.dto.*;
// import lombok.RequiredArgsConstructor;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.time.OffsetDateTime;
// import java.util.List;

// @RestController
// @RequestMapping("/api/querellas")
// @RequiredArgsConstructor
// public class QuerellaController {

// private final QuerellaService service;

// @PostMapping
// public ResponseEntity<QuerellaResponse> crear(@RequestBody QuerellaCreateDTO
// dto) {
// return ResponseEntity.ok(service.crear(dto));
// }

// @PutMapping("/{id}/asignacion")
// public ResponseEntity<QuerellaResponse> asignarInspeccion(
// @PathVariable Long id, @RequestBody AsignarInspeccionDTO dto) {
// return ResponseEntity.ok(service.asignarInspeccion(id,
// dto.getInspeccionId()));
// }

// @PutMapping("/{id}/estado")
// public ResponseEntity<QuerellaResponse> cambiarEstado(
// @PathVariable Long id, @RequestBody CambioEstadoDTO dto) {
// return ResponseEntity.ok(
// service.cambiarEstado(id, dto.getNuevoEstado(), dto.getMotivo(),
// dto.getUsuarioId()));
// }

// @GetMapping("/{id}")
// public ResponseEntity<QuerellaResponse> detalle(@PathVariable Long id) {
// return ResponseEntity.ok(service.detalle(id));
// }

// @GetMapping
// public ResponseEntity<List<Querella>> buscar(
// @RequestParam(required = false) String q,
// @RequestParam(required = false) Long estadoId,
// @RequestParam(required = false) Long inspeccionId,
// @RequestParam(required = false) Long comunaId,
// @RequestParam(required = false) @DateTimeFormat(iso =
// DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
// @RequestParam(required = false) @DateTimeFormat(iso =
// DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta
// ) {
// return ResponseEntity.ok(service.buscar(q, estadoId, inspeccionId, comunaId,
// desde, hasta));
// }
// }
