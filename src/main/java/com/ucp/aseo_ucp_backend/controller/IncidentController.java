package com.ucp.aseo_ucp_backend.controller; // O tu paquete

import java.util.List;
import java.util.Map; // Necesario si devuelves DTO en GET

import org.springframework.http.HttpStatus; // Importa la entidad para el servicio
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // Importa HttpStatus
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.IncidentDto;
import com.ucp.aseo_ucp_backend.dto.IncidentRequest; // Importa Map
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.service.IncidentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('cleaning_staff', 'admin')")
    public ResponseEntity<?> createIncident(@Valid @RequestBody IncidentRequest request) { // <-- ResponseEntity<?>
        Incident createdIncident = incidentService.createIncident(request); // Llama al servicio

        // --- ASEGÚRATE DE TENER ESTE BLOQUE ---
        // Crea un cuerpo de respuesta simple
        Map<String, Object> responseBody = Map.of(
            "message", "Incidente reportado exitosamente",
            "incidentId", createdIncident.getId() // Devuelve el ID generado
        );
        // Usa el estado HTTP 201 Created y el cuerpo simple
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        // --- FIN DEL BLOQUE IMPORTANTE ---
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')") // Asumiendo solo admin ve la lista
    public ResponseEntity<List<IncidentDto>> getIncidents() { // Asegúrate de devolver DTOs
        List<IncidentDto> incidents = incidentService.getAllIncidents(); // Llama al método que devuelve DTOs
        return ResponseEntity.ok(incidents);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin')") // Solo admin puede cambiar estado
    public ResponseEntity<IncidentDto> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        String newStatusStr = statusUpdate.get("status");
        if (newStatusStr == null) {
            // Devuelve error si el body no tiene la clave "status"
            return ResponseEntity.badRequest().build(); 
        }

        try {
            Incident.Status newStatus = Incident.Status.valueOf(newStatusStr); // Convierte String a Enum
            IncidentDto updatedIncident = incidentService.updateIncidentStatus(id, newStatus);
            return ResponseEntity.ok(updatedIncident);
        } catch (IllegalArgumentException e) {
            // Devuelve error si el valor del status no es válido (ej. "en_proceso")
            return ResponseEntity.badRequest().build(); 
        }
    }
}