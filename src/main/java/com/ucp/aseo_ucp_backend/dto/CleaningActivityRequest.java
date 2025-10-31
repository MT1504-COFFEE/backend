package com.ucp.aseo_ucp_backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CleaningActivityRequest {
    @NotNull
    private Long bathroomId;

    // IDs de las entidades CleaningArea
    private List<Long> areasCleanedIds;

    // IDs de las entidades Supply
    private List<Long> suppliesRefilledIds;

    private String observations;

    // Lista de URLs de las fotos ya subidas
    private List<String> photos;
}