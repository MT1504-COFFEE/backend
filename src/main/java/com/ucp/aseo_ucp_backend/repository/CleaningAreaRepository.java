package com.ucp.aseo_ucp_backend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.CleaningArea; // Importa Set

@Repository
public interface CleaningAreaRepository extends JpaRepository<CleaningArea, Long> {
    Set<CleaningArea> findByIdIn(List<Long> ids); // Método para buscar por lista de IDs
}