package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Querella;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List; 

public interface QuerellaRepository extends JpaRepository<Querella, Long> {

    @Query(
        value = """
        SELECT q.*
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
        WHERE
            (
                :qTexto IS NULL
                OR q.radicado_interno ILIKE CONCAT('%', :qTexto, '%')
                OR q.id_local         ILIKE CONCAT('%', :qTexto, '%')
                OR q.direccion        ILIKE CONCAT('%', :qTexto, '%')
                OR q.descripcion      ILIKE CONCAT('%', :qTexto, '%')
            )
        AND
            (
                :estadoNombre IS NULL
                OR est.estado_nombre = :estadoNombre
            )
        AND
            (
                :inspeccionId IS NULL
                OR q.inspeccion_id = :inspeccionId
            )
        AND
            (
                :comunaId IS NULL
                OR q.comuna_id = :comunaId
            )
        AND
            (
                q.creado_en >= COALESCE(CAST(:desde AS timestamptz), q.creado_en)
                AND q.creado_en <= COALESCE(CAST(:hasta AS timestamptz), q.creado_en)
            )
        ORDER BY q.creado_en DESC
        """,
        countQuery = """
        SELECT COUNT(*)
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
        WHERE
            (
                :qTexto IS NULL
                OR q.radicado_interno ILIKE CONCAT('%', :qTexto, '%')
                OR q.id_local         ILIKE CONCAT('%', :qTexto, '%')
                OR q.direccion        ILIKE CONCAT('%', :qTexto, '%')
                OR q.descripcion      ILIKE CONCAT('%', :qTexto, '%')
            )
        AND
            (
                :estadoNombre IS NULL
                OR est.estado_nombre = :estadoNombre
            )
        AND
            (
                :inspeccionId IS NULL
                OR q.inspeccion_id = :inspeccionId
            )
        AND
            (
                :comunaId IS NULL
                OR q.comuna_id = :comunaId
            )
        AND
            (
                q.creado_en >= COALESCE(CAST(:desde AS timestamptz), q.creado_en)
                AND q.creado_en <= COALESCE(CAST(:hasta AS timestamptz), q.creado_en)
            )
        """,
        nativeQuery = true
    )
    Page<Querella> buscarBandejaPaginada(
            @Param("qTexto")        String qTexto,
            @Param("estadoNombre")  String estadoNombre,
            @Param("inspeccionId")  Long inspeccionId,
            @Param("comunaId")      Long comunaId,
            @Param("desde")         OffsetDateTime desde,
            @Param("hasta")         OffsetDateTime hasta,
            Pageable pageable
    );

    boolean existsByRadicadoInterno(String radicadoInterno);




    @Query("""
        select q from Querella q
        where q.id <> :idBase
          and lower(q.direccion) = lower(:direccion)
          and (:comunaId is null or q.comuna.id = :comunaId)
          and (:temaId   is null or q.tema.id   = :temaId)
          and q.creadoEn between :desde and :hasta
    """)
    List<Querella> buscarPosiblesDuplicados(
            @Param("idBase") Long idBase,
            @Param("direccion") String direccion,
            @Param("comunaId") Long comunaId,
            @Param("temaId") Long temaId,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta
    );

}
