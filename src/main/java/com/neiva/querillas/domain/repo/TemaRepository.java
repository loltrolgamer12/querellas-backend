package com.neiva.querillas.domain.repo;

import com.neiva.querillas.domain.entity.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemaRepository extends JpaRepository<Tema, Long> {
}
