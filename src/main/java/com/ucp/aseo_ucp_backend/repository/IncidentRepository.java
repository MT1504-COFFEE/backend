package com.ucp.aseo_ucp_backend.repository; 

import java.util.List;
import java.util.Optional; // <-- AÑADE ESTE IMPORT

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; // <-- AÑADE ESTE IMPORT
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.Incident; 

@Repository 
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    @Query("SELECT i FROM Incident i " +
           "LEFT JOIN FETCH i.bathroom b " +
           "LEFT JOIN FETCH i.reportedBy u " +
           "LEFT JOIN FETCH i.assignedTo a " + 
           "LEFT JOIN FETCH b.building bu " + 
           "LEFT JOIN FETCH b.floor fl " + 
           "ORDER BY i.createdAt DESC")
    List<Incident> findAllWithDetails();

    // --- AÑADE ESTE NUEVO MÉTODO ---
    @Query("SELECT i FROM Incident i " +
           "LEFT JOIN FETCH i.bathroom b " +
           "LEFT JOIN FETCH i.reportedBy u " +
           "LEFT JOIN FETCH i.assignedTo a " +
           "LEFT JOIN FETCH b.building bu " +
           "LEFT JOIN FETCH b.floor fl " +
           "WHERE i.id = :id")
    Optional<Incident> findByIdWithDetails(@Param("id") Long id);
    // -------------------------------
}