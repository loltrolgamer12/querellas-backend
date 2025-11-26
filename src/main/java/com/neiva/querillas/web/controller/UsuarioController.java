package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.service.UsuarioService;
import com.neiva.querillas.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios?page={page}&size={size}&role={role}
     * Listar usuarios con paginación y filtro opcional por rol
     */
    @GetMapping
    public ResponseEntity<PaginaUsuarioResponse> listar(
            @RequestParam(required = false) RolUsuario role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/usuarios - role: {}, page: {}, size: {}", role, page, size);
        PaginaUsuarioResponse response = usuarioService.listar(role, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/usuarios/{id}
     * Obtener detalle de un usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}", id);
        UsuarioResponse response = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/usuarios
     * Crear un nuevo usuario
     */
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateDTO dto) {
        log.info("POST /api/usuarios - email: {}", dto.getEmail());
        UsuarioResponse response = usuarioService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT /api/usuarios/{id}
     * Actualizar un usuario existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {
        log.info("PUT /api/usuarios/{}", id);
        UsuarioResponse response = usuarioService.actualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/usuarios/{id}/estado
     * Cambiar estado de un usuario (bloquear/desbloquear)
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoUsuarioDTO dto) {
        log.info("PUT /api/usuarios/{}/estado - nuevo estado: {}", id, dto.getNuevoEstado());
        UsuarioResponse response = usuarioService.cambiarEstado(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/usuarios/{id}
     * Eliminar un usuario (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{}", id);
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
