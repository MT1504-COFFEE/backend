package com.ucp.aseo_ucp_backend.dto; 

import com.ucp.aseo_ucp_backend.entity.Incident;
import lombok.Data;
import java.time.format.DateTimeFormatter; 
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
    private String createdAt; 
    private String resolvedAt; 
    private Long bathroomId;
    private String bathroomName;
    private String buildingName;
    private String floorName; 
    private Long reportedById;
    private String reportedByName;
    private List<String> photos;

    // --- NUEVOS CAMPOS ---
    private Long assignedToId;
    private String assignedToName;
    // -------------------

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static IncidentDto fromEntity(Incident incident) {
        if (incident == null) return null;
        IncidentDto dto = new IncidentDto();
        dto.setId(incident.getId());
        dto.setTitle(incident.getTitle());
        // ... (resto de campos: description, priority, status) ...
        dto.setDescription(incident.getDescription());
        dto.setPriority(incident.getPriority());
        dto.setStatus(incident.getStatus());

        // ... (lógica de fechas createdAt y resolvedAt) ...
        if (incident.getCreatedAt() != null) {
            dto.setCreatedAt(incident.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        if (incident.getResolvedAt() != null) {
            dto.setResolvedAt(incident.getResolvedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }

        // ... (lógica de bathroom y reportedBy) ...
        if (incident.getBathroom() != null) {
            dto.setBathroomId(incident.getBathroom().getId());
            dto.setBathroomName(incident.getBathroom().getName());
            if (incident.getBathroom().getBuilding() != null) {
                dto.setBuildingName(incident.getBathroom().getBuilding().getName());
            }
            if (incident.getBathroom().getFloor() != null) {
                // Asumo que floorName es el número de piso
                dto.setFloorName(String.valueOf(incident.getBathroom().getFloor().getFloorNumber()));
            }
        }
        if (incident.getReportedBy() != null) {
            dto.setReportedById(incident.getReportedBy().getId());
            dto.setReportedByName(incident.getReportedBy().getFullName());
        }

        // --- LÓGICA PARA NUEVOS CAMPOS ---
        if (incident.getAssignedTo() != null) {
            dto.setAssignedToId(incident.getAssignedTo().getId());
            dto.setAssignedToName(incident.getAssignedTo().getFullName());
        }
        // --------------------------------

        // ... (lógica de fotos) ...
        if (incident.getPhotos() != null && !incident.getPhotos().isEmpty() && !incident.getPhotos().equals("[]")) {
           try {
               dto.setPhotos(objectMapper.readValue(incident.getPhotos(), new TypeReference<List<String>>() {}));
           } catch (Exception e) {
               dto.setPhotos(List.of());
           }
        } else {
           dto.setPhotos(List.of());
        }

        return dto;
    }
}