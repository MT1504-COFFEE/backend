package com.ucp.aseo_ucp_backend.dto; // O tu paquete dto

import com.ucp.aseo_ucp_backend.entity.Incident;
import lombok.Data;
// import java.time.LocalDateTime; // No se usa directamente aquí
import java.time.format.DateTimeFormatter; // Necesario para formatear
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Data
public class IncidentDto {
    private Long id;
    private String title;
    private String description;
    private Incident.Priority priority;
    private Incident.Status status;
    private String createdAt; // <- Cambiado a String
    private String resolvedAt; // <- Cambiado a String
    private Long bathroomId;
    private String bathroomName;
    private String buildingName;
    private String floorName; // O el tipo correcto
    private Long reportedById;
    private String reportedByName;
    private List<String> photos;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Método estático para convertir Entidad a DTO
    public static IncidentDto fromEntity(Incident incident) {
        if (incident == null) return null;
        IncidentDto dto = new IncidentDto();
        dto.setId(incident.getId());
        dto.setTitle(incident.getTitle());
        dto.setDescription(incident.getDescription());
        dto.setPriority(incident.getPriority());
        dto.setStatus(incident.getStatus());

        // --- Formateo de Fechas ---
        if (incident.getCreatedAt() != null) {
            dto.setCreatedAt(incident.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        } else {
             dto.setCreatedAt(null);
        }
        if (incident.getResolvedAt() != null) {
            dto.setResolvedAt(incident.getResolvedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        } else {
            dto.setResolvedAt(null);
        }
        // -------------------------

        if (incident.getBathroom() != null) {
            dto.setBathroomId(incident.getBathroom().getId());
            dto.setBathroomName(incident.getBathroom().getName());
            if (incident.getBathroom().getBuilding() != null) {
                dto.setBuildingName(incident.getBathroom().getBuilding().getName());
            }
             // Lógica para floorName si es necesario
            // if (incident.getBathroom().getFloor() != null) { ... }
        }

        if (incident.getReportedBy() != null) {
            dto.setReportedById(incident.getReportedBy().getId());
            dto.setReportedByName(incident.getReportedBy().getFullName());
        }

        // Deserializar fotos
        if (incident.getPhotos() != null && !incident.getPhotos().isEmpty() && !incident.getPhotos().equals("[]")) {
           try {
               dto.setPhotos(objectMapper.readValue(incident.getPhotos(), new TypeReference<List<String>>() {}));
           } catch (Exception e) {
               System.err.println("Error deserializando fotos del incidente " + incident.getId() + ": " + e.getMessage());
               dto.setPhotos(List.of());
           }
        } else {
           dto.setPhotos(List.of());
        }

        return dto;
    }
}