package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Barrio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarrioRepository extends JpaRepository<Barrio, Long> {
    List<Barrio> findByComunaId(Long comunaId);
}
