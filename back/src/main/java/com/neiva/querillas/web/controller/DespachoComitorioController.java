package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.DespachoComitorioService;
import com.neiva.querillas.domain.service.DespachoExcelService;
import com.neiva.querillas.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/despachos-comisorios")
@RequiredArgsConstructor
@Slf4j
public class DespachoComitorioController {

    private final DespachoComitorioService despachoService;
    private final DespachoExcelService despachoExcelService;

    /**
     * Crear nuevo despacho comisorio
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public ResponseEntity<DespachoComitorioResponse> crear(@Valid @RequestBody DespachoComitorioCreateDTO dto) {
        log.info("POST /api/despachos-comisorios - número: {}", dto.getNumeroDespacho());
        DespachoComitorioResponse response = despachoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener despacho por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<DespachoComitorioResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/despachos-comisorios/{}", id);
        DespachoComitorioResponse response = despachoService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar todos los despachos con paginación
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<Page<DespachoComitorioResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRecibido") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("GET /api/despachos-comisorios - page: {}, size: {}", page, size);
        Page<DespachoComitorioResponse> response = despachoService.listar(page, size, sortBy, direction);
        return ResponseEntity.ok(response);
    }

    /**
     * Listar despachos pendientes (sin devolver)
     */
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<List<DespachoComitorioResponse>> listarPendientes() {
        log.info("GET /api/despachos-comisorios/pendientes");
        List<DespachoComitorioResponse> response = despachoService.listarPendientes();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar despachos devueltos
     */
    @GetMapping("/devueltos")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<List<DespachoComitorioResponse>> listarDevueltos() {
        log.info("GET /api/despachos-comisorios/devueltos");
        List<DespachoComitorioResponse> response = despachoService.listarDevueltos();
        return ResponseEntity.ok(response);
    }

    /**
     * Listar despachos por inspector
     */
    @GetMapping("/inspector/{inspectorId}")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<List<DespachoComitorioResponse>> listarPorInspector(@PathVariable Long inspectorId) {
        log.info("GET /api/despachos-comisorios/inspector/{}", inspectorId);
        List<DespachoComitorioResponse> response = despachoService.listarPorInspector(inspectorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar despacho
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public ResponseEntity<DespachoComitorioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DespachoComitorioUpdateDTO dto
    ) {
        log.info("PUT /api/despachos-comisorios/{}", id);
        DespachoComitorioResponse response = despachoService.actualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Asignar inspector a despacho
     */
    @PutMapping("/{id}/asignar-inspector")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public ResponseEntity<DespachoComitorioResponse> asignarInspector(
            @PathVariable Long id,
            @RequestParam Long inspectorId,
            @RequestParam(required = false) Long asignadoPorId
    ) {
        log.info("PUT /api/despachos-comisorios/{}/asignar-inspector - inspectorId: {}", id, inspectorId);
        DespachoComitorioResponse response = despachoService.asignarInspector(id, inspectorId, asignadoPorId);
        return ResponseEntity.ok(response);
    }

    /**
     * Marcar despacho como devuelto
     */
    @PutMapping("/{id}/marcar-devuelto")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR','INSPECTOR')")
    public ResponseEntity<DespachoComitorioResponse> marcarComoDevuelto(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fechaDevolucion
    ) {
        log.info("PUT /api/despachos-comisorios/{}/marcar-devuelto", id);
        DespachoComitorioResponse response = despachoService.marcarComoDevuelto(id, fechaDevolucion);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar despacho
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/despachos-comisorios/{}", id);
        despachoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generar reporte JSON por rango de fechas
     */
    @GetMapping("/reporte")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public ResponseEntity<List<DespachoComitorioReporteDTO>> generarReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        log.info("GET /api/despachos-comisorios/reporte - desde: {}, hasta: {}", desde, hasta);

        OffsetDateTime desdeDateTime = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<DespachoComitorioReporteDTO> response = despachoService.generarReporte(desdeDateTime, hastaDateTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Generar reporte en Excel (Formato FOR-GGOJ-81)
     */
    @GetMapping("/reporte/excel")
    @PreAuthorize("hasAnyRole('DIRECTOR','AUXILIAR')")
    public ResponseEntity<byte[]> generarReporteExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) throws IOException {
        log.info("GET /api/despachos-comisorios/reporte/excel - desde: {}, hasta: {}", desde, hasta);

        byte[] excelBytes = despachoExcelService.generarReporteExcel(desde, hasta);

        // Generar nombre de archivo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String filename = String.format("despachos_comisorios_%s_%s.xlsx",
                desde.format(formatter),
                hasta.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
