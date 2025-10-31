package com.ucp.aseo_ucp_backend.repository; // <- Tu paquete de repositorios

import java.util.List; // <- Tu paquete de entidades

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Para el JOIN FETCH opcional
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.Incident; // Para el JOIN FETCH opcional

@Repository // <- Anotación @Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Método opcional para cargar eficientemente las relaciones Bathroom y User
    @Query("SELECT i FROM Incident i LEFT JOIN FETCH i.bathroom b LEFT JOIN FETCH i.reportedBy u ORDER BY i.createdAt DESC")
    List<Incident> findAllWithDetails();

    // Puedes añadir más métodos de consulta aquí si los necesitas
    // Por ejemplo, para buscar por status o prioridad:
    // List<Incident> findByStatus(Incident.Status status);
    // List<Incident> findByPriority(Incident.Priority priority);
}