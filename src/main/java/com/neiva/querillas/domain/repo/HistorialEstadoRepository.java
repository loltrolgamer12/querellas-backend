package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    @Query(value = """
        SELECT e.nombre
        FROM historial_estado he
                 JOIN estado e ON e.id = he.estado_id
        WHERE he.modulo = :modulo
          AND he.caso_id = :casoId
        ORDER BY he.creado_en DESC NULLS LAST
        LIMIT 1
        """, nativeQuery = true)
    Optional<String> findUltimoEstadoNombre(
            @Param("modulo") String modulo,
            @Param("casoId") Long casoId
    );



    @Query("""
      SELECT he FROM HistorialEstado he
      WHERE he.modulo = :modulo AND he.casoId = :casoId
      ORDER BY he.creadoEn DESC
  """)
  List<HistorialEstado> findByModuloAndCasoIdOrderByCreadoEnDesc(
          @Param("modulo") String modulo,
          @Param("casoId") Long casoId
  );
}



// package com.neiva.querillas.domain.repo;

// import com.neiva.querillas.domain.entity.HistorialEstado;
// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.Optional;

// public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
//     Optional<HistorialEstado> findFirstByModuloAndCasoIdOrderByCreadoEnDesc(String modulo, Long casoId);
// }
