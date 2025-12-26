package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Long> {
  Optional<Estado> findByModuloAndNombre(String modulo, String nombre);

  @Query("SELECT e FROM Estado e WHERE e.modulo = :modulo ORDER BY e.nombre ASC")
  List<Estado> findByModulo(String modulo);
}
