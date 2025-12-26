// package com.neiva.querillas.domain.repo;

// import com.neiva.querillas.domain.entity.EstadoTransicion;
// import org.springframework.data.jpa.repository.JpaRepository;

// public interface EstadoTransicionRepository extends JpaRepository<EstadoTransicion, Long> {
//   boolean existsByModuloAndDesde_IdAndHacia_Id(String modulo, Long desdeId, Long haciaId);
// }



package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.EstadoTransicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstadoTransicionRepository extends JpaRepository<EstadoTransicion, Long> {

    @Query("""
        select (count(t) > 0) from EstadoTransicion t
        where t.modulo = :modulo and t.desde.id = :desdeId and t.hacia.id = :haciaId
    """)
    boolean existeTransicion(
        @Param("modulo") String modulo, 
        @Param("desdeId") Long desdeId, 
        @Param("haciaId") Long haciaId);
}
