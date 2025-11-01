package com.ucp.aseo_ucp_backend.repository; 

import java.util.List; 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.Incident; 

@Repository 
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // --- CONSULTA MODIFICADA ---
    @Query("SELECT i FROM Incident i " +
           "LEFT JOIN FETCH i.bathroom b " +
           "LEFT JOIN FETCH i.reportedBy u " +
           "LEFT JOIN FETCH i.assignedTo a " + // <-- AÑADIR ESTA LÍNEA
           "LEFT JOIN FETCH b.building bu " + // <-- AÑADIR JOIN A BUILDING
           "LEFT JOIN FETCH b.floor fl " + // <-- AÑADIR JOIN A FLOOR
           "ORDER BY i.createdAt DESC")
    List<Incident> findAllWithDetails();
    // -------------------------
}