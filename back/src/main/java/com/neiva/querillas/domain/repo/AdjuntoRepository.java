package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Adjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    List<Adjunto> findByQuerellaIdOrderByCreadoEnDesc(Long querellaId);
}
