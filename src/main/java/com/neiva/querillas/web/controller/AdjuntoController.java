package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.entity.Adjunto;
import com.neiva.querillas.domain.service.AdjuntoService;
import com.neiva.querillas.web.dto.AdjuntoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/querellas/{querellaId}/adjuntos")
@RequiredArgsConstructor
@Slf4j
public class AdjuntoController {

    private final AdjuntoService adjuntoService;

    /**
     * GET /api/querellas/{id}/adjuntos
     * Listar adjuntos de una querella
     */
    @GetMapping
    public ResponseEntity<List<AdjuntoResponse>> listar(@PathVariable Long querellaId) {
        log.info("GET /api/querellas/{}/adjuntos", querellaId);
        List<AdjuntoResponse> response = adjuntoService.listar(querellaId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/querellas/{id}/adjuntos
     * Subir adjunto
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdjuntoResponse> subir(
            @PathVariable Long querellaId,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String descripcion) {
        log.info("POST /api/querellas/{}/adjuntos - archivo: {}", querellaId, archivo.getOriginalFilename());
        AdjuntoResponse response = adjuntoService.subir(querellaId, archivo, descripcion);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/querellas/{querellaId}/adjuntos/{adjuntoId}/descargar
     * Descargar adjunto
     */
    @GetMapping("/{adjuntoId}/descargar")
    public ResponseEntity<Resource> descargar(
            @PathVariable Long querellaId,
            @PathVariable Long adjuntoId) {
        log.info("GET /api/querellas/{}/adjuntos/{}/descargar", querellaId, adjuntoId);

        Resource resource = adjuntoService.descargar(querellaId, adjuntoId);
        Adjunto adjunto = adjuntoService.obtenerPorId(adjuntoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(adjunto.getTipoArchivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + adjunto.getNombreArchivo() + "\"")
                .body(resource);
    }

    /**
     * DELETE /api/querellas/{querellaId}/adjuntos/{adjuntoId}
     * Eliminar adjunto
     */
    @DeleteMapping("/{adjuntoId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long querellaId,
            @PathVariable Long adjuntoId) {
        log.info("DELETE /api/querellas/{}/adjuntos/{}", querellaId, adjuntoId);
        adjuntoService.eliminar(querellaId, adjuntoId);
        return ResponseEntity.noContent().build();
    }
}
