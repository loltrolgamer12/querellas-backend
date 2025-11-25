package com.neiva.querillas.web.controller;

import com.neiva.querillas.domain.service.QuerellaService;
import com.neiva.querillas.web.dto.QuerellaReporteDTO;
import com.neiva.querillas.web.dto.DashboardQuerellasResumen;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;     
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final QuerellaService querellaService;

    @GetMapping("/querellas-trimestral")
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR')")
    public ResponseEntity<List<QuerellaReporteDTO>> reporteTrimestral(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long inspeccionId
    ) {
        List<QuerellaReporteDTO> datos = querellaService.generarReporteTrimestral(desde, hasta, inspeccionId);
        return ResponseEntity.ok(datos);
    }




    
    @GetMapping("/dashboard-querellas")
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public ResponseEntity<DashboardQuerellasResumen> dashboardQuerellas(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        // Convertimos LocalDate a OffsetDateTime UTC (o el offset que uses)
        OffsetDateTime odDesde = (desde == null)
                ? null
                : desde.atStartOfDay().atOffset(ZoneOffset.UTC);

        OffsetDateTime odHasta = (hasta == null)
                ? null
                : hasta.plusDays(1).atStartOfDay().minusNanos(1).atOffset(ZoneOffset.UTC);

        DashboardQuerellasResumen dto = querellaService.obtenerDashboard(odDesde, odHasta);
        return ResponseEntity.ok(dto);
    }



}
