package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.DespachoComisorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DespachoComisarioRepository extends JpaRepository<DespachoComisorio, Long> {

    // Buscar por corregimiento
    Page<DespachoComisorio> findByCorregimientoAsignadoId(Long corregimientoId, Pageable pageable);

    // Buscar por rango de fechas de recibido
    @Query("SELECT d FROM DespachoComisorio d WHERE d.fechaRecibido BETWEEN :desde AND :hasta")
    List<DespachoComisorio> findByFechaRecibidoBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    // Buscar pendientes de devolución
    @Query("SELECT d FROM DespachoComisorio d WHERE d.fechaDevolucion IS NULL")
    Page<DespachoComisorio> findPendientesDevolucion(Pageable pageable);

    // Buscar por entidad procedente
    Page<DespachoComisorio> findByEntidadProcedenteContainingIgnoreCase(String entidad, Pageable pageable);
}
