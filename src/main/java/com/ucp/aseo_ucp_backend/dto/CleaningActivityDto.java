package com.ucp.aseo_ucp_backend.dto; // O tu paquete dto

import com.ucp.aseo_ucp_backend.entity.CleaningActivity;
import com.ucp.aseo_ucp_backend.entity.CleaningArea;
import com.ucp.aseo_ucp_backend.entity.Supply;
import lombok.Data;
// import java.time.LocalDateTime; // No usado directamente aquí
import java.time.format.DateTimeFormatter;
import java.util.List;
// import java.util.Set; // No usado directamente aquí
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException; // Importa la específica
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Data
public class CleaningActivityDto {
    private Long id;
    private String createdAt; // String ISO 8601
    private String observations;
    private Long userId;
    private String userName;
    private Long bathroomId;
    private String bathroomName;
    private String buildingName;
    // private String floorName;
    private List<String> areasCleanedNames;
    private List<String> suppliesRefilledNames;
    private List<String> photos;

    // Considera inyectar ObjectMapper como Bean si lo configuras globalmente
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CleaningActivityDto fromEntity(CleaningActivity activity) {
        if (activity == null) return null;
        CleaningActivityDto dto = new CleaningActivityDto();
        dto.setId(activity.getId());
        dto.setObservations(activity.getObservations());

        if (activity.getCreatedAt() != null) {
            dto.setCreatedAt(activity.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        } else {
            dto.setCreatedAt(null);
        }

        if (activity.getUser() != null) {
            dto.setUserId(activity.getUser().getId());
            dto.setUserName(activity.getUser().getFullName());
        }

        if (activity.getBathroom() != null) {
            dto.setBathroomId(activity.getBathroom().getId());
            dto.setBathroomName(activity.getBathroom().getName());
            if (activity.getBathroom().getBuilding() != null) {
                dto.setBuildingName(activity.getBathroom().getBuilding().getName());
            }
        }

        if (activity.getAreasCleaned() != null) {
            dto.setAreasCleanedNames(activity.getAreasCleaned().stream()
                                           .map(CleaningArea::getName)
                                           .collect(Collectors.toList()));
        } else {
             dto.setAreasCleanedNames(List.of());
        }

         if (activity.getSuppliesRefilled() != null) {
            dto.setSuppliesRefilledNames(activity.getSuppliesRefilled().stream()
                                              .map(Supply::getName)
                                              .collect(Collectors.toList()));
        } else {
            dto.setSuppliesRefilledNames(List.of());
        }

        // Deserializar fotos
        if (activity.getPhotos() != null && !activity.getPhotos().isEmpty() && !activity.getPhotos().equals("[]")) {
           try {
               dto.setPhotos(objectMapper.readValue(activity.getPhotos(), new TypeReference<List<String>>() {}));
           
           // --- CORRECCIÓN AQUÍ ---
           } catch (Exception e) { // <- Captura genérica de Excepción
           // -------------------------
           
               // Usa un logger real en producción
               System.err.println("Error deserializando fotos de actividad " + activity.getId() + ": " + e.getMessage());
               dto.setPhotos(List.of());
           }
        } else {
           dto.setPhotos(List.of());
        }

        return dto;
    }
}