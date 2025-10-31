package com.ucp.aseo_ucp_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus; // O un CleaningActivityDto para la respuesta
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // Para autorización por rol/autoridad
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.CleaningActivityDto;
import com.ucp.aseo_ucp_backend.dto.CleaningActivityRequest;
import com.ucp.aseo_ucp_backend.entity.CleaningActivity;
import com.ucp.aseo_ucp_backend.service.CleaningActivityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cleaning-activities")
@RequiredArgsConstructor
public class CleaningActivityController {

    private final CleaningActivityService cleaningActivityService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('cleaning_staff', 'admin')")
    public ResponseEntity<?> createActivity(@Valid @RequestBody CleaningActivityRequest request) {
        CleaningActivity createdActivity = cleaningActivityService.createActivity(request);

        // --- CAMBIO AQUÍ ---
        // Devuelve solo un mensaje de éxito y el ID, en lugar de toda la entidad
        Map<String, Object> responseBody = Map.of(
            "message", "Actividad creada exitosamente",
            "activityId", createdActivity.getId()
        );
    return ResponseEntity.status(HttpStatus.CREATED).body(responseBody); // Usa 201 Created
    // --- FIN DEL CAMBIO ---
}

    @GetMapping
    @PreAuthorize("hasAuthority('admin')") // Asumiendo solo admin ve historial
    public ResponseEntity<List<CleaningActivityDto>> getActivities() { // Cambia tipo de retorno
        List<CleaningActivityDto> activities = cleaningActivityService.getAllActivities();
    return ResponseEntity.ok(activities);
}

    // ... otros endpoints (GET por ID, PUT, DELETE) ...
}