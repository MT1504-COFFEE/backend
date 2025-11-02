package com.ucp.aseo_ucp_backend.repository; 

import java.util.List;
import java.util.Optional; // <-- ASEGÚRATE DE TENER ESTE IMPORT

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param; // <-- ASEGÚRATE DE TENER ESTE IMPORT
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.Incident; 

@Repository 
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /**
     * Esta es la consulta que usa el Dashboard de Admin.
     * Carga TODO (baño, reportador, asignado, edificio, piso) en una sola consulta.
     */
    @Query("SELECT i FROM Incident i " +
           "LEFT JOIN FETCH i.bathroom b " +
           "LEFT JOIN FETCH i.reportedBy u " +
           "LEFT JOIN FETCH i.assignedTo a " + 
           "LEFT JOIN FETCH b.building bu " + 
           "LEFT JOIN FETCH b.floor fl " + 
           "ORDER BY i.createdAt DESC")
    List<Incident> findAllWithDetails();

    /**
     * Esta es la consulta que SOLUCIONA tu error.
     * Carga todos los detalles de un SOLO incidente por su ID.
     * La usaremos justo después de crear un incidente para poder enviar el correo.
     */
    @Query("SELECT i FROM Incident i " +
           "LEFT JOIN FETCH i.bathroom b " +
           "LEFT JOIN FETCH i.reportedBy u " +
           "LEFT JOIN FETCH i.assignedTo a " +
           "LEFT JOIN FETCH b.building bu " +
           "LEFT JOIN FETCH b.floor fl " +
           "WHERE i.id = :id")
    Optional<Incident> findByIdWithDetails(@Param("id") Long id);
}