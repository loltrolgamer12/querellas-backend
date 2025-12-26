package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Comunicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComunicacionRepository extends JpaRepository<Comunicacion, Long> {

    List<Comunicacion> findByQuerellaIdOrderByCreadoEnDesc(Long querellaId);
}
