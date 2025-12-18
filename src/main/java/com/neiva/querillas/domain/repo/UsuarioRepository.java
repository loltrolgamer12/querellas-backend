package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.EstadoUsuario;
import com.neiva.querillas.domain.model.RolUsuario;
import com.neiva.querillas.domain.model.ZonaInspector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:rol IS NULL OR u.rol = :rol) " +
           "ORDER BY u.creadoEn DESC")
    Page<Usuario> findAllByRol(@Param("rol") RolUsuario rol, Pageable pageable);

    /**
     * Buscar usuarios por rol y estado (para listar inspectores activos)
     */
    List<Usuario> findByRolAndEstado(RolUsuario rol, EstadoUsuario estado);

    /**
     * Buscar usuarios por rol, zona y estado (para listar inspectores por zona)
     */
    List<Usuario> findByRolAndZonaAndEstado(RolUsuario rol, ZonaInspector zona, EstadoUsuario estado);
}
