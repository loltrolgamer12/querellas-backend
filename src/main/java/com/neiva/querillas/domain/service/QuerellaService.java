package com.neiva.querillas.domain.service;

import com.neiva.querillas.domain.entity.*;
import com.neiva.querillas.domain.model.Naturaleza;
import com.neiva.querillas.domain.repo.*;
import com.neiva.querillas.web.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import com.neiva.querillas.web.dto.DashboardQuerellasResumen;
import java.util.HashMap;
import java.util.Map;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class QuerellaService {

    private final QuerellaRepository querellaRepo;
    private final EstadoRepository estadoRepo;
    private final HistorialEstadoRepository historialRepo;
    private final UsuarioRepository usuarioRepo;
    private final TemaRepository temaRepo;
    private final ComunaRepository comunaRepo;

    private final EstadoTransicionRepository estadoTransicionRepo; 

    
    @PersistenceContext
    private EntityManager entityManager;

    // ======================
    // CREAR QUERELLA
    // ======================
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR')")
    public QuerellaResponse crear(QuerellaCreateDTO dto) {

        // Validación correcta con enum
        Naturaleza nat = dto.getNaturaleza();
        if (nat == null) {
            throw new IllegalArgumentException("naturaleza debe ser OFICIO, PERSONA o ANONIMA");
        }

        Tema tema = null;
        if (dto.getTemaId() != null) {
            tema = temaRepo.findById(dto.getTemaId())
                    .orElseThrow(() -> new EntityNotFoundException("Tema no existe"));
        }

        Comuna comuna = null;
        if (dto.getComunaId() != null) {
            comuna = comunaRepo.findById(dto.getComunaId())
                    .orElseThrow(() -> new EntityNotFoundException("Comuna no existe"));
        }

        Usuario inspector = null;
        if (dto.getInspectorAsignadoId() != null) {
            inspector = usuarioRepo.findById(dto.getInspectorAsignadoId())
                    .orElseThrow(() -> new EntityNotFoundException("Inspector no existe"));
        }

        Estado estadoInicial = estadoRepo
                .findByModuloAndNombre("QUERELLA", "RECIBIDA")
                .orElseThrow(() -> new IllegalStateException("Estado inicial RECIBIDA no existe"));

        Querella q = Querella.builder()
                .radicadoInterno(generarRadicadoUnico())
                .direccion(dto.getDireccion())
                .descripcion(dto.getDescripcion())
                .tema(tema)
                .naturaleza(nat)
                .inspectorAsignado(inspector)
                .asignadoPor(dto.getAsignadoPorId() != null ?
                    usuarioRepo.findById(dto.getAsignadoPorId()).orElse(null) : null)
                .comuna(comuna)
                .idAlcaldia(null)
                .esMigrado(false)
                .creadoPor(null)
                .creadoEn(OffsetDateTime.now())
                .actualizadoEn(OffsetDateTime.now())
                .idLocal(null)
                .querellanteNombre(null)
                .querellanteContacto(null)
                .barrio(dto.getBarrio())
                .generoQuerellante(dto.getGeneroQuerellante())
                .generoQuerellado(dto.getGeneroQuerellado())
                .normasAplicables(dto.getNormasAplicables())
                .observaciones(dto.getObservaciones())
                .tieneFallo(null)
                .tieneApelacion(null)
                .archivado(null)
                .materializacionMedida(null)
                .build();

        q = querellaRepo.save(q);

        HistorialEstado he = HistorialEstado.builder()
                .modulo("QUERELLA")
                .casoId(q.getId())
                .estado(estadoInicial)
                .motivo("apertura")
                .usuarioId(null)
                .creadoEn(OffsetDateTime.now())
                .build();
        historialRepo.save(he);

        return toResponse(q);
    }

    // ======================
    // DETALLE
    // ======================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public QuerellaResponse detalle(Long id) {
        Querella q = querellaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada"));
        return toResponse(q);
    }

    // ======================
    // LISTAR BANDEJA (filtros + paginación + ordenamiento)
    // ======================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public PaginaQuerellaResponse listarBandeja(
            String qTexto,
            String estadoNombre,
            Long inspectorId,
            String temaNombre,
            Long comunaId,
            OffsetDateTime desde,
            OffsetDateTime hasta,
            Integer page,
            Integer size,
            String sortParam
    ) {

        int pageSafe = (page == null || page < 0) ? 0 : page;
        int sizeSafe = (size == null || size <= 0) ? 10 : size;

        // -------- sort ----------
        String orderByCampo = "q.creado_en";
        String orderByDir   = "DESC";

        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            if (parts.length >= 1) {
                switch (parts[0]) {
                    case "creadoEn"        -> orderByCampo = "q.creado_en";
                    case "radicadoInterno" -> orderByCampo = "q.radicado_interno";
                    case "estadoActual"    -> orderByCampo = "estado_actual";
                    default -> { }
                }
            }
            if (parts.length >= 2) {
                if ("ASC".equalsIgnoreCase(parts[1])) {
                    orderByDir = "ASC";
                } else if ("DESC".equalsIgnoreCase(parts[1])) {
                    orderByDir = "DESC";
                }
            }
        }

        String sqlBase = """
            FROM querella q
            LEFT JOIN tema t ON t.id = q.tema_id
            LEFT JOIN LATERAL (
                SELECT e.nombre AS estado_nombre
                FROM historial_estado he
                         JOIN estado e ON e.id = he.estado_id
                WHERE he.modulo = 'QUERELLA'
                  AND he.caso_id = q.id
                ORDER BY he.creado_en DESC NULLS LAST
                LIMIT 1
            ) est ON TRUE
            WHERE
                (
                    CAST(:qTexto AS text) IS NULL
                    OR q.radicado_interno ILIKE CONCAT('%', CAST(:qTexto AS text), '%')
                    OR q.id_local         ILIKE CONCAT('%', CAST(:qTexto AS text), '%')
                    OR q.direccion        ILIKE CONCAT('%', CAST(:qTexto AS text), '%')
                    OR q.descripcion      ILIKE CONCAT('%', CAST(:qTexto AS text), '%')
                )
            AND
                (
                    CAST(:estadoNombre AS text) IS NULL
                    OR est.estado_nombre = CAST(:estadoNombre AS text)
                )
            AND
                (
                    CAST(:inspectorId AS bigint) IS NULL
                    OR q.inspector_asignado_id = CAST(:inspectorId AS bigint)
                )
            AND
                (
                    CAST(:temaNombre AS text) IS NULL
                    OR t.nombre ILIKE CONCAT('%', CAST(:temaNombre AS text), '%')
                )
            AND
                (
                    CAST(:comunaId AS bigint) IS NULL
                    OR q.comuna_id = CAST(:comunaId AS bigint)
                )
            AND
                (
                    q.creado_en >= COALESCE(CAST(:desde AS timestamptz), q.creado_en)
                    AND q.creado_en <= COALESCE(CAST(:hasta AS timestamptz), q.creado_en)
                )
            """;

        String sqlData =
                "SELECT q.*, " +
                "       est.estado_nombre AS estado_actual " +
                sqlBase +
                " ORDER BY " + orderByCampo + " " + orderByDir +
                " LIMIT :limit " +
                " OFFSET :offset ";

        String sqlCount =
                "SELECT COUNT(*) " +
                sqlBase;

        var queryData  = entityManager.createNativeQuery(sqlData, Querella.class);
        var queryCount = entityManager.createNativeQuery(sqlCount);

        Object qTextoParam       = (qTexto == null || qTexto.isBlank()) ? null : qTexto;
        Object estadoParam       = (estadoNombre == null || estadoNombre.isBlank()) ? null : estadoNombre;
        Object inspectorParam    = inspectorId;
        Object temaParam         = (temaNombre == null || temaNombre.isBlank()) ? null : temaNombre;
        Object comunaParam       = comunaId;

        queryData.setParameter("qTexto",        qTextoParam);
        queryData.setParameter("estadoNombre",  estadoParam);
        queryData.setParameter("inspectorId",   inspectorParam);
        queryData.setParameter("temaNombre",    temaParam);
        queryData.setParameter("comunaId",      comunaParam);
        queryData.setParameter("desde",         desde);
        queryData.setParameter("hasta",         hasta);
        queryData.setParameter("limit",         sizeSafe);
        queryData.setParameter("offset",        pageSafe * sizeSafe);

        queryCount.setParameter("qTexto",        qTextoParam);
        queryCount.setParameter("estadoNombre",  estadoParam);
        queryCount.setParameter("inspectorId",   inspectorParam);
        queryCount.setParameter("temaNombre",    temaParam);
        queryCount.setParameter("comunaId",      comunaParam);
        queryCount.setParameter("desde",         desde);
        queryCount.setParameter("hasta",         hasta);

        @SuppressWarnings("unchecked")
        List<Querella> filas = queryData.getResultList();

        Number totalCountNum = (Number) queryCount.getSingleResult();
        long totalElements   = totalCountNum.longValue();

        List<QuerellaResponse> items = filas.stream()
                .map(this::toResponse)
                .toList();

        return PaginaQuerellaResponse.builder()
                .items(items)
                .page(pageSafe)
                .size(sizeSafe)
                .totalItems(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / (double) sizeSafe))
                .sort(orderByCampo + " " + orderByDir)
                .build();
    }

    // ======================
    // ASIGNAR / REASIGNAR INSPECTOR
    // ======================
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','INSPECTOR')")
    public QuerellaResponse asignarInspector(Long id, AsignarInspectorDTO dto, Long asignadoPorId) {

        Querella q = querellaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada"));

        Usuario inspector = usuarioRepo.findById(dto.getInspectorId())
                .orElseThrow(() -> new EntityNotFoundException("Inspector no encontrado"));

        Usuario asignadoPor = null;
        if (asignadoPorId != null) {
            asignadoPor = usuarioRepo.findById(asignadoPorId).orElse(null);
        }

        q.setInspectorAsignado(inspector);
        q.setAsignadoPor(asignadoPor);
        q.setActualizadoEn(OffsetDateTime.now());
        q = querellaRepo.save(q);

        return toResponse(q);
    }

    /**
     * Método interno para asignar inspector (sin validación de seguridad)
     * Usado por el servicio de asignación automática
     */
    @Transactional
    public QuerellaResponse asignarInspectorInterno(Long querellaId, Long inspectorId, Long asignadoPorId) {

        Querella q = querellaRepo.findById(querellaId)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada con ID: " + querellaId));

        Usuario inspector = usuarioRepo.findById(inspectorId)
                .orElseThrow(() -> new EntityNotFoundException("Inspector no encontrado con ID: " + inspectorId));

        Usuario asignadoPor = null;
        if (asignadoPorId != null) {
            asignadoPor = usuarioRepo.findById(asignadoPorId).orElse(null);
        }

        q.setInspectorAsignado(inspector);
        q.setAsignadoPor(asignadoPor);
        q.setActualizadoEn(OffsetDateTime.now());
        q = querellaRepo.save(q);

        return toResponse(q);
    }

    // ======================
    // CAMBIAR ESTADO
    // ======================
    @Transactional
    @PreAuthorize("hasAnyRole('DIRECTORA','INSPECTOR')")
    public QuerellaResponse cambiarEstado(Long id, CambioEstadoDTO dto) {
        if (dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            throw new IllegalArgumentException("Motivo es obligatorio");
        }
    
        Querella q = querellaRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Querella no encontrada"));
    
        // 1) Nombre del estado actual (último en historial)
        String estadoActualNombre = obtenerEstadoActualNombre(q.getId()); // puede ser null si algo raro pasó
    
        // 2) Si ya está en ese estado, no repetimos
        if (estadoActualNombre != null &&
            estadoActualNombre.equalsIgnoreCase(dto.getNuevoEstado())) {
            return toResponse(q);
        }
    
        // 3) Buscar el estado destino (lo necesitas sí o sí)
        Estado nuevoEstado = estadoRepo
                .findByModuloAndNombre("QUERELLA", dto.getNuevoEstado())
                .orElseThrow(() -> new EntityNotFoundException("Estado destino no existe"));
    
        // 4) Validar la transición contra la tabla estado_transicion
        if (estadoActualNombre != null) {
            // cargamos el Estado actual por nombre
            Estado estadoActual = estadoRepo
                    .findByModuloAndNombre("QUERELLA", estadoActualNombre)
                    .orElseThrow(() -> new IllegalStateException(
                            "Estado actual " + estadoActualNombre + " no existe en catálogo"));
    
            boolean permitida = estadoTransicionRepo.existeTransicion(
                    "QUERELLA",
                    estadoActual.getId(),   // desdeId
                    nuevoEstado.getId()     // haciaId
            );
    
            if (!permitida) {
                throw new IllegalStateException(
                        "Transición no permitida de " + estadoActualNombre +
                        " a " + dto.getNuevoEstado()
                );
            }
        }
        // Si estadoActualNombre es null (no hay historial), podrías lanzar error
        // o dejarlo pasar. Aquí lo dejamos pasar como caso excepcional.
    
        // 5) Guardar en historial
        HistorialEstado he = HistorialEstado.builder()
                .modulo("QUERELLA")
                .casoId(q.getId())
                .estado(nuevoEstado)
                .motivo(dto.getMotivo())
                .usuarioId(dto.getUsuarioId())
                .creadoEn(OffsetDateTime.now())
                .build();
    
        historialRepo.save(he);
    
        q.setActualizadoEn(OffsetDateTime.now());
        q = querellaRepo.save(q);
    
        return toResponse(q);
    }
    

    // ======================
    // HISTORIAL ESTADOS
    // ======================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public List<HistorialEstadoDTO> historialEstados(Long querellaId) {
        List<HistorialEstado> lista = historialRepo
                .findByModuloAndCasoIdOrderByCreadoEnDesc("QUERELLA", querellaId);

        return lista.stream()
                .map(he -> new HistorialEstadoDTO(
                        he.getEstado().getNombre(),
                        he.getMotivo(),
                        he.getUsuarioId(),
                        he.getCreadoEn()
                ))
                .toList();
    }

    // ======================
    // REPORTE TRIMESTRAL
    // ======================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR')")
    public List<QuerellaReporteDTO> generarReporteTrimestral(
            LocalDate desde,
            LocalDate hasta,
            Long inspectorId
    ) {
        OffsetDateTime desdeDateTime = desde.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        String jpql = """
            SELECT q
            FROM Querella q
            LEFT JOIN FETCH q.inspectorAsignado i
            LEFT JOIN FETCH q.tema t
            LEFT JOIN FETCH q.comuna c
            WHERE q.creadoEn >= :desde
              AND q.creadoEn < :hasta
              AND (:inspectorId IS NULL OR q.inspectorAsignado.id = :inspectorId)
            """;

        var query = entityManager.createQuery(jpql, Querella.class);
        query.setParameter("desde", desdeDateTime);
        query.setParameter("hasta", hastaDateTime);
        query.setParameter("inspectorId", inspectorId);

        List<Querella> resultado = query.getResultList();

        return resultado.stream()
                .map(q -> QuerellaReporteDTO.builder()
                        .id(q.getId())
                        .fechaRadicado(q.getCreadoEn())
                        .inspectorNombre(q.getInspectorAsignado() == null ? null : q.getInspectorAsignado().getNombre())
                        .radicadoInterno(q.getRadicadoInterno())
                        .temaNombre(q.getTema() == null ? null : q.getTema().getNombre())
                        .generoQuerellante(q.getGeneroQuerellante())
                        .generoQuerellado(q.getGeneroQuerellado())
                        .normasAplicables(q.getNormasAplicables())
                        .barrio(q.getBarrio())
                        .comunaNombre(q.getComuna() == null ? null : q.getComuna().getNombre())
                        .tieneFallo(q.getTieneFallo())
                        .tieneApelacion(q.getTieneApelacion())
                        .archivado(q.getArchivado())
                        .materializacionMedida(q.getMaterializacionMedida())
                        .observaciones(q.getObservaciones())
                        .build()
                )
                .toList();
    }



    // ======================
    // DASHBOARD
    // ======================
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public DashboardQuerellasResumen obtenerDashboard(OffsetDateTime desde, OffsetDateTime hasta) {

        // ---- Filtro de fechas reutilizable (mismo estilo que listarBandeja) ----
        String filtroFechas = """
            WHERE
                q.creado_en >= COALESCE(:desde, q.creado_en)
            AND q.creado_en <= COALESCE(:hasta, q.creado_en)
        """;

        // ---------- 1) Total de querellas ----------
        String sqlTotal = "SELECT COUNT(*) FROM querella q " + filtroFechas;

        Number totalNum = (Number) entityManager
                .createNativeQuery(sqlTotal)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();

        long totalQuerellas = totalNum.longValue();

        // ---------- 2) Conteo por estado ACTUAL ----------
        String sqlPorEstado = """
            SELECT est.estado_nombre, COUNT(*)
            FROM querella q
            LEFT JOIN LATERAL (
                SELECT e.nombre AS estado_nombre
                FROM historial_estado he
                         JOIN estado e ON e.id = he.estado_id
                WHERE he.modulo = 'QUERELLA'
                  AND he.caso_id = q.id
                ORDER BY he.creado_en DESC NULLS LAST
                LIMIT 1
            ) est ON TRUE
        """ + filtroFechas +
                " GROUP BY est.estado_nombre";

        @SuppressWarnings("unchecked")
        var rowsEstado = entityManager.createNativeQuery(sqlPorEstado)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        Map<String, Long> porEstado = new HashMap<>();
        for (Object rowObj : rowsEstado) {
            Object[] row = (Object[]) rowObj;
            String nombreEstado = (String) row[0];   // puede ser null si no hay historial
            Number count = (Number) row[1];
            porEstado.put(nombreEstado == null ? "SIN_ESTADO" : nombreEstado, count.longValue());
        }

        // ---------- 3) Conteo por inspector ----------
        String sqlPorInspector = """
            SELECT u.nombre, COUNT(*)
            FROM querella q
                 JOIN usuarios u ON u.id = q.inspector_asignado_id
        """ + filtroFechas +
                " GROUP BY u.nombre";

        @SuppressWarnings("unchecked")
        var rowsInspector = entityManager.createNativeQuery(sqlPorInspector)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        Map<String, Long> porInspector = new HashMap<>();
        for (Object rowObj : rowsInspector) {
            Object[] row = (Object[]) rowObj;
            String nombreInsp = (String) row[0];
            Number count = (Number) row[1];
            porInspector.put(nombreInsp, count.longValue());
        }

        // ---------- 4) Conteo por naturaleza ----------
        String sqlPorNaturaleza = """
            SELECT q.naturaleza, COUNT(*)
            FROM querella q
        """ + filtroFechas +
                " GROUP BY q.naturaleza";

        @SuppressWarnings("unchecked")
        var rowsNaturaleza = entityManager.createNativeQuery(sqlPorNaturaleza)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        Map<String, Long> porNaturaleza = new HashMap<>();
        for (Object rowObj : rowsNaturaleza) {
            Object[] row = (Object[]) rowObj;
            String nat = (String) row[0]; // OFICIO, PERSONA, ANONIMA
            Number count = (Number) row[1];
            porNaturaleza.put(nat, count.longValue());
        }

        return DashboardQuerellasResumen.builder()
                .totalQuerellas(totalQuerellas)
                .porEstado(porEstado)
                .porInspector(porInspector)
                .porNaturaleza(porNaturaleza)
                .build();
    }


    // ======================
    // LISTAR BANDEJA
    // ======================

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('DIRECTORA','AUXILIAR','INSPECTOR')")
    public List<QuerellaResponse> posiblesDuplicados(Long querellaId) {

        Querella base = querellaRepo.findById(querellaId)
                .orElseThrow(() -> new EntityNotFoundException("Querella base no encontrada"));

        // Ventana de tiempo: 180 días antes y 30 días después (ajústalo si quieres)
        OffsetDateTime centro = base.getCreadoEn();
        OffsetDateTime desde = centro.minus(180, ChronoUnit.DAYS);
        OffsetDateTime hasta = centro.plus(30, ChronoUnit.DAYS);

        Long comunaId = (base.getComuna() == null) ? null : base.getComuna().getId();
        Long temaId   = (base.getTema()   == null) ? null : base.getTema().getId();

        List<Querella> candidatos = querellaRepo.buscarPosiblesDuplicados(
                base.getId(),
                base.getDireccion(),
                comunaId,
                temaId,
                desde,
                hasta
        );

        return candidatos.stream()
                .map(this::toResponse)
                .toList();
    }




    // ======================
    // HELPERS
    // ======================

    private String obtenerEstadoActualNombre(Long querellaId) {
        return historialRepo.findUltimoEstadoNombre("QUERELLA", querellaId)
                .orElse(null);
    }

    private String generarRadicado() {
        Number next = (Number) entityManager
                .createNativeQuery("SELECT nextval('seq_radicado_querella')")
                .getSingleResult();

        long numero = next.longValue();
        int year = OffsetDateTime.now().getYear();
        return "Q-" + year + "-" + String.format("%06d", numero);
    }

    private String generarRadicadoUnico() {
        for (int i = 0; i < 3; i++) {
            String rad = generarRadicado();
            Boolean exists = (Boolean) entityManager
                    .createQuery("SELECT COUNT(q)>0 FROM Querella q WHERE q.radicadoInterno=:rad")
                    .setParameter("rad", rad)
                    .getSingleResult();
            if (!exists) return rad;
        }
        throw new IllegalStateException("No fue posible generar radicado único");
    }

    private QuerellaResponse toResponse(Querella q) {
        return QuerellaResponse.builder()
                .id(q.getId())
                .radicadoInterno(q.getRadicadoInterno())
                .idLocal(q.getIdLocal())
                .direccion(q.getDireccion())
                .descripcion(q.getDescripcion())
                .naturaleza(q.getNaturaleza() == null ? null : q.getNaturaleza().name())
                .temaId(q.getTema() == null ? null : q.getTema().getId())
                .temaNombre(q.getTema() == null ? null : q.getTema().getNombre())
                .inspectorAsignadoId(q.getInspectorAsignado() == null ? null : q.getInspectorAsignado().getId())
                .inspectorAsignadoNombre(q.getInspectorAsignado() == null ? null : q.getInspectorAsignado().getNombre())
                .inspectorAsignadoZona(q.getInspectorAsignado() == null ? null :
                    (q.getInspectorAsignado().getZona() == null ? null : q.getInspectorAsignado().getZona().name()))
                .asignadoPorId(q.getAsignadoPor() == null ? null : q.getAsignadoPor().getId())
                .asignadoPorNombre(q.getAsignadoPor() == null ? null : q.getAsignadoPor().getNombre())
                .comunaId(q.getComuna() == null ? null : q.getComuna().getId())
                .comunaNombre(q.getComuna() == null ? null : q.getComuna().getNombre())
                .barrio(q.getBarrio())
                .generoQuerellante(q.getGeneroQuerellante())
                .generoQuerellado(q.getGeneroQuerellado())
                .normasAplicables(q.getNormasAplicables())
                .observaciones(q.getObservaciones())
                .tieneFallo(q.getTieneFallo())
                .tieneApelacion(q.getTieneApelacion())
                .archivado(q.getArchivado())
                .materializacionMedida(q.getMaterializacionMedida())
                .estadoActual(obtenerEstadoActualNombre(q.getId()))
                .creadoEn(q.getCreadoEn())
                .build();
    }
}
