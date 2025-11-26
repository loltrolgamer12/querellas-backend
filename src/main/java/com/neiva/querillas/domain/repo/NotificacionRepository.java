package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId " +
           "AND (:leida IS NULL OR n.leida = :leida) " +
           "ORDER BY n.creadoEn DESC")
    Page<Notificacion> findByUsuarioIdAndLeida(
            @Param("usuarioId") Long usuarioId,
            @Param("leida") Boolean leida,
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leida = false")
    long countNoLeidasByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Long usuarioId);
}
