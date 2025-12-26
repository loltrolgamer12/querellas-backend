package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoComitorioRepository extends JpaRepository<DespachoComisorio, Long> {

    /**
     * Buscar por número de despacho
     */
    Optional<DespachoComisorio> findByNumeroDespacho(String numeroDespacho);

    /**
     * Buscar por inspector asignado
     */
    List<DespachoComisorio> findByInspectorAsignadoId(Long inspectorId);

    /**
     * Buscar por entidad procedente
     */
    List<DespachoComisorio> findByEntidadProcedenteContainingIgnoreCase(String entidad);

    /**
     * Buscar por rango de fechas
     */
    @Query("SELECT d FROM DespachoComisorio d WHERE d.fechaRecibido >= :desde AND d.fechaRecibido <= :hasta ORDER BY d.fechaRecibido DESC")
    List<DespachoComisorio> findByFechaRecibidoBetween(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta
    );

    /**
     * Despachos pendientes (sin devolver)
     */
    @Query("SELECT d FROM DespachoComisorio d WHERE d.fechaDevolucion IS NULL ORDER BY d.fechaRecibido ASC")
    List<DespachoComisorio> findPendientes();

    /**
     * Despachos devueltos
     */
    @Query("SELECT d FROM DespachoComisorio d WHERE d.fechaDevolucion IS NOT NULL ORDER BY d.fechaDevolucion DESC")
    List<DespachoComisorio> findDevueltos();

    /**
     * Verificar si existe un despacho con ese número
     */
    boolean existsByNumeroDespacho(String numeroDespacho);
}
