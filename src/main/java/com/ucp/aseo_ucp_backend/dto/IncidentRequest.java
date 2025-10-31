package com.ucp.aseo_ucp_backend.dto;

import java.util.List; // Importa el Enum

import com.ucp.aseo_ucp_backend.entity.Incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidentRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull // Asegúrate que la prioridad venga
    private Incident.Priority priority; // Usa el Enum directamente si el frontend envía 'low', 'medium', 'high'

    @NotNull
    private Long bathroomId;

    // Lista de URLs de las fotos ya subidas
    private List<String> photos;
}