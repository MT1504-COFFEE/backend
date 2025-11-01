package com.ucp.aseo_ucp_backend.dto;

import com.ucp.aseo_ucp_backend.entity.Incident;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIncidentStatusRequest {
    @NotNull
    private Incident.Status status;
    
    // Este ID es opcional, solo se envía al pasar a "in_progress"
    private Long assignedUserId; 
}