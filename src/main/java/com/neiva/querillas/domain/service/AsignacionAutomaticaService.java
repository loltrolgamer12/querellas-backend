package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.ConfiguracionSistema;
import com.neiva.querillas.domain.entity.Querella;
import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.repo.ConfiguracionSistemaRepository;
import com.neiva.querillas.domain.repo.QuerellaRepository;
import com.neiva.querillas.domain.repo.UsuarioRepository;
import com.neiva.querillas.web.dto.AsignacionAutomaticaRequest;
import com.neiva.querillas.web.dto.AsignacionAutomaticaResponse;
import com.neiva.querillas.web.dto.QuerellaResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para asignación automática de querellas usando algoritmo Round-Robin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionAutomaticaService {

    private static final String CLAVE_ULTIMO_INSPECTOR_ID = "ROUND_ROBIN_ULTIMO_INSPECTOR_ID";

    private final QuerellaRepository querellaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConfiguracionSistemaRepository configRepository;
    private final QuerellaService querellaService;

    /**
     * Asigna automáticamente múltiples querellas a inspectores usando round-robin
     *
     * @param request Contiene lista de IDs de querellas y asignadoPorId
     * @return Response con lista de querellas asignadas
     */
    @Transactional
    public AsignacionAutomaticaResponse asignarAutomaticamente(AsignacionAutomaticaRequest request) {
        log.info("Iniciando asignación automática de {} querellas", request.getQuerellaIds().size());

        // 1. Obtener lista de inspectores activos ordenados por ID
        List<Usuario> inspectores = usuarioRepository.findByRolAndEstado(
                RolUsuario.INSPECTOR,
                EstadoUsuario.ACTIVO
        );

        if (inspectores.isEmpty()) {
            throw new IllegalStateException("No hay inspectores activos disponibles para asignación");
        }

        // Ordenar inspectores por ID para garantizar consistencia
        inspectores.sort((a, b) -> a.getId().compareTo(b.getId()));

        log.info("Inspectores activos disponibles: {}", inspectores.size());

        // 2. Obtener el último inspector asignado (índice en la lista)
        int ultimoIndice = obtenerUltimoIndiceAsignado(inspectores);
        log.info("Último índice usado: {}", ultimoIndice);

        // 3. Asignar cada querella al siguiente inspector (round-robin)
        List<QuerellaResponse> querellasAsignadas = new ArrayList<>();
        int indiceActual = ultimoIndice;

        for (Long querellaId : request.getQuerellaIds()) {
            // Siguiente inspector en el ciclo
            indiceActual = (indiceActual + 1) % inspectores.size();
            Usuario inspector = inspectores.get(indiceActual);

            log.info("Asignando querella {} al inspector {} (índice {})",
                    querellaId, inspector.getId(), indiceActual);

            // Asignar la querella usando el servicio existente
            QuerellaResponse querellaAsignada = querellaService.asignarInspectorInterno(
                    querellaId,
                    inspector.getId(),
                    request.getAsignadoPorId()
            );

            querellasAsignadas.add(querellaAsignada);
        }

        // 4. Guardar el último índice usado
        guardarUltimoIndice(indiceActual, inspectores);

        log.info("Asignación automática completada. {} querellas asignadas. Último índice: {}",
                querellasAsignadas.size(), indiceActual);

        return AsignacionAutomaticaResponse.builder()
                .querellasAsignadas(querellasAsignadas)
                .totalAsignadas(querellasAsignadas.size())
                .ultimoInspectorId(inspectores.get(indiceActual).getId())
                .build();
    }

    /**
     * Obtiene el último índice usado en el round-robin
     * Si no existe configuración, empieza desde -1 (el próximo será 0)
     */
    private int obtenerUltimoIndiceAsignado(List<Usuario> inspectores) {
        ConfiguracionSistema config = configRepository.findByClave(CLAVE_ULTIMO_INSPECTOR_ID)
                .orElse(null);

        if (config == null || config.getValor() == null) {
            log.info("No hay configuración previa de round-robin, iniciando desde -1");
            return -1; // Siguiente será 0
        }

        try {
            Long ultimoInspectorId = Long.parseLong(config.getValor());

            // Buscar el índice del inspector en la lista actual
            for (int i = 0; i < inspectores.size(); i++) {
                if (inspectores.get(i).getId().equals(ultimoInspectorId)) {
                    return i;
                }
            }

            // Si el inspector ya no está activo, empezar desde -1
            log.warn("Inspector con ID {} ya no está activo, reiniciando round-robin", ultimoInspectorId);
            return -1;

        } catch (NumberFormatException e) {
            log.error("Error al parsear último inspector ID: {}", config.getValor(), e);
            return -1;
        }
    }

    /**
     * Guarda el último índice usado (como ID del inspector)
     */
    private void guardarUltimoIndice(int indice, List<Usuario> inspectores) {
        Long inspectorId = inspectores.get(indice).getId();

        ConfiguracionSistema config = configRepository.findByClave(CLAVE_ULTIMO_INSPECTOR_ID)
                .orElse(ConfiguracionSistema.builder()
                        .clave(CLAVE_ULTIMO_INSPECTOR_ID)
                        .descripcion("ID del último inspector asignado en round-robin")
                        .build());

        config.setValor(inspectorId.toString());
        configRepository.save(config);

        log.info("Guardado último inspector ID: {}", inspectorId);
    }
}
