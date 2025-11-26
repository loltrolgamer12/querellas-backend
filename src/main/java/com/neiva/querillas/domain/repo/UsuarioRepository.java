package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Usuario;
import com.neiva.querillas.domain.model.RolUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:rol IS NULL OR u.rol = :rol) " +
           "ORDER BY u.creadoEn DESC")
    Page<Usuario> findAllByRol(@Param("rol") RolUsuario rol, Pageable pageable);
}
