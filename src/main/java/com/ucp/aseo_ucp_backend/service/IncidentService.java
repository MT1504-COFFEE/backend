package com.ucp.aseo_ucp_backend.service;

import java.util.List;

import com.ucp.aseo_ucp_backend.dto.IncidentDto;
import com.ucp.aseo_ucp_backend.dto.IncidentRequest; // Importa la entidad
import com.ucp.aseo_ucp_backend.entity.Incident;

public interface IncidentService {
    Incident createIncident(IncidentRequest request);
    List<IncidentDto> getAllIncidents();

    // --- AÑADE ESTE MÉTODO ---
    /**
     * Actualiza el estado de un incidente.
     * @param id El ID del incidente a actualizar.
     * @param newStatus El nuevo estado.
     * @return El DTO del incidente actualizado.
     */
    IncidentDto updateIncidentStatus(Long id, Incident.Status newStatus);
}