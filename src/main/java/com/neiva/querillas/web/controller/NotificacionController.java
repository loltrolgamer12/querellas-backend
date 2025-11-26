package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.NotificacionService;
import com.neiva.querillas.web.dto.ListaNotificacionesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionController {

    private final NotificacionService notificacionService;

    /**
     * GET /api/notificaciones?leida={true/false}&page={page}&size={size}
     * Listar notificaciones del usuario actual
     */
    @GetMapping
    public ResponseEntity<ListaNotificacionesResponse> listar(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/notificaciones - leida: {}, page: {}, size: {}", leida, page, size);
        ListaNotificacionesResponse response = notificacionService.listar(leida, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/notificaciones/{id}/leer
     * Marcar notificación como leída
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        log.info("PUT /api/notificaciones/{}/leer", id);
        notificacionService.marcarComoLeida(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/notificaciones/leer-todas
     * Marcar todas las notificaciones como leídas
     */
    @PutMapping("/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLeidas() {
        log.info("PUT /api/notificaciones/leer-todas");
        Map<String, Integer> result = notificacionService.marcarTodasComoLeidas();
        return ResponseEntity.ok(result);
    }

    /**
     * DELETE /api/notificaciones/{id}
     * Eliminar notificación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/notificaciones/{}", id);
        notificacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
