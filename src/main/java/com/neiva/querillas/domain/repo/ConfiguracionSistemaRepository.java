package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {

    /**
     * Buscar configuración por clave
     */
    Optional<ConfiguracionSistema> findByClave(String clave);

    /**
     * Verificar si existe una configuración con la clave dada
     */
    boolean existsByClave(String clave);
}
