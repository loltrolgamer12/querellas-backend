package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.repo.*;
import com.neiva.querillas.web.dto.ItemSimpleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private final InspeccionRepository inspeccionRepo;
    private final ComunaRepository comunaRepo;
    private final EstadoRepository estadoRepo;

    @GetMapping("/inspecciones")
    public List<ItemSimpleDTO> listarInspecciones() {
        return inspeccionRepo.findAll().stream()
                .map(i -> new ItemSimpleDTO(i.getId(), i.getNombre()))
                .toList();
    }

    @GetMapping("/comunas")
    public List<ItemSimpleDTO> listarComunas() {
        return comunaRepo.findAll().stream()
                .map(c -> new ItemSimpleDTO(c.getId(), c.getNombre()))
                .toList();
    }

    @GetMapping("/estados")
    public List<ItemSimpleDTO> listarEstados() {
        return estadoRepo.findByModulo("QUERELLA").stream()
                .map(e -> new ItemSimpleDTO(e.getNombre(), e.getNombre()))
                .toList();
    }
}
