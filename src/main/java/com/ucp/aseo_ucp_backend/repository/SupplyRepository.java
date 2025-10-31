package com.ucp.aseo_ucp_backend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.Supply; // Importa Set

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
     Set<Supply> findByIdIn(List<Long> ids); // Método para buscar por lista de IDs
}