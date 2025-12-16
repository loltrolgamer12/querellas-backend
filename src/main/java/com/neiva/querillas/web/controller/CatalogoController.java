package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.repo.*;
import com.neiva.querillas.domain.service.CatalogoService;
import com.neiva.querillas.web.dto.CatalogoDTO;
import com.neiva.querillas.web.dto.ItemSimpleDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
@Slf4j
public class CatalogoController {

    private final ComunaRepository comunaRepo;
    private final EstadoRepository estadoRepo;
    private final TemaRepository temaRepo;
    private final CatalogoService catalogoService;

    // ==================== TEMAS ====================

    @GetMapping("/temas")
    public List<ItemSimpleDTO> listarTemas() {
        return temaRepo.findAll().stream()
                .map(t -> new ItemSimpleDTO(t.getId(), t.getNombre()))
                .toList();
    }

    @PostMapping("/temas")
    public ResponseEntity<ItemSimpleDTO> crearTema(@Valid @RequestBody CatalogoDTO dto) {
        log.info("POST /api/catalogos/temas - nombre: {}", dto.getNombre());
        ItemSimpleDTO response = catalogoService.crearTema(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/temas/{id}")
    public ResponseEntity<ItemSimpleDTO> actualizarTema(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoDTO dto) {
        log.info("PUT /api/catalogos/temas/{}", id);
        ItemSimpleDTO response = catalogoService.actualizarTema(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/temas/{id}")
    public ResponseEntity<Void> eliminarTema(@PathVariable Long id) {
        log.info("DELETE /api/catalogos/temas/{}", id);
        catalogoService.eliminarTema(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== COMUNAS ====================

    @GetMapping("/comunas")
    public List<ItemSimpleDTO> listarComunas() {
        return comunaRepo.findAll().stream()
                .map(c -> new ItemSimpleDTO(c.getId(), c.getNombre()))
                .toList();
    }

    @PostMapping("/comunas")
    public ResponseEntity<ItemSimpleDTO> crearComuna(@Valid @RequestBody CatalogoDTO dto) {
        log.info("POST /api/catalogos/comunas - nombre: {}", dto.getNombre());
        ItemSimpleDTO response = catalogoService.crearComuna(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/comunas/{id}")
    public ResponseEntity<ItemSimpleDTO> actualizarComuna(
            @PathVariable Long id,
            @Valid @RequestBody CatalogoDTO dto) {
        log.info("PUT /api/catalogos/comunas/{}", id);
        ItemSimpleDTO response = catalogoService.actualizarComuna(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comunas/{id}")
    public ResponseEntity<Void> eliminarComuna(@PathVariable Long id) {
        log.info("DELETE /api/catalogos/comunas/{}", id);
        catalogoService.eliminarComuna(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ESTADOS ====================

    @GetMapping("/estados")
    public List<ItemSimpleDTO> listarEstados() {
        return estadoRepo.findByModulo("QUERELLA").stream()
                .map(e -> new ItemSimpleDTO(e.getNombre(), e.getNombre()))
                .toList();
    }
}
