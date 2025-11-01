package com.ucp.aseo_ucp_backend.controller; // O tu paquete

import java.util.List;
import java.util.Map; // Necesario si devuelves DTO en GET

import org.springframework.http.HttpStatus; // Importa la entidad para el servicio
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Importa HttpStatus
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.IncidentDto;
import com.ucp.aseo_ucp_backend.dto.IncidentRequest;
import com.ucp.aseo_ucp_backend.dto.UpdateIncidentStatusRequest;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.exception.ResourceNotFoundException; // Importa Map
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
    @PreAuthorize("hasAuthority('admin')") 
    public ResponseEntity<IncidentDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) { // <-- USA EL DTO
        
        try {
            IncidentDto updatedIncident = incidentService.updateIncidentStatus(id, request);
            return ResponseEntity.ok(updatedIncident);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            // Captura errores de lógica de negocio (ej. usuario no encontrado)
            // Es mejor si usas tu @ControllerAdvice (GlobalExceptionHandler)
            // Pero esto funciona como fallback.
            return ResponseEntity.badRequest().body(null); // O un DTO de error
        }
    }
}