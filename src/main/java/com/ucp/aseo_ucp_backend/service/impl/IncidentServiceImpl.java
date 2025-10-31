package com.ucp.aseo_ucp_backend.service.impl; // O tu paquete

import java.time.LocalDateTime; // Importa el DTO
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.dto.IncidentDto;
import com.ucp.aseo_ucp_backend.dto.IncidentRequest;
import com.ucp.aseo_ucp_backend.entity.Bathroom;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.exception.ResourceNotFoundException; // Importa Collectors
import com.ucp.aseo_ucp_backend.repository.BathroomRepository;
import com.ucp.aseo_ucp_backend.repository.IncidentRepository;
import com.ucp.aseo_ucp_backend.service.AuthService;
import com.ucp.aseo_ucp_backend.service.IncidentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final BathroomRepository bathroomRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Incident createIncident(IncidentRequest request) {
        // ... (código de createIncident como antes) ...
         User currentUser = authService.getCurrentUser();
         if (currentUser == null) {
             throw new RuntimeException("Usuario no autenticado para reportar incidente.");
         }

         Bathroom bathroom = bathroomRepository.findById(request.getBathroomId())
                 .orElseThrow(() -> new RuntimeException("Baño no encontrado con ID: " + request.getBathroomId()));

         Incident incident = new Incident();
         incident.setTitle(request.getTitle());
         incident.setDescription(request.getDescription());
         incident.setPriority(request.getPriority());
         incident.setStatus(Incident.Status.pending);
         incident.setBathroom(bathroom);
         incident.setReportedBy(currentUser);

         try {
             if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
                 incident.setPhotos(objectMapper.writeValueAsString(request.getPhotos()));
             } else {
                  incident.setPhotos("[]");
             }
         } catch (JsonProcessingException e) {
             throw new RuntimeException("Error al procesar la lista de fotos para el incidente", e);
         }

         return incidentRepository.save(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentDto> getAllIncidents() { // <- ASEGÚRATE que devuelva List<IncidentDto>
        // Usa la consulta con JOIN FETCH del repositorio para eficiencia
        List<Incident> incidents = incidentRepository.findAllWithDetails();

        // Mapea la lista de Entidades a una lista de DTOs
        return incidents.stream()
                        .map(IncidentDto::fromEntity) // Llama al método estático del DTO
                        .collect(Collectors.toList()); // Recolecta en una nueva lista
    }

    @Override
    @Transactional
    public IncidentDto updateIncidentStatus(Long id, Incident.Status newStatus) {
        // 1. Encuentra el incidente o lanza error
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // 2. Actualiza el estado
        incident.setStatus(newStatus);

        // 3. Si el nuevo estado es "resolved", guarda la fecha de resolución
        if (newStatus == Incident.Status.resolved) {
            incident.setResolvedAt(LocalDateTime.now());
        } else {
            incident.setResolvedAt(null); // Quita la fecha si se re-abre
        }

        // 4. Guarda y devuelve el DTO actualizado
        Incident updatedIncident = incidentRepository.save(incident);
        
        // 5. Recarga con 'findAllWithDetails' para obtener nombres, etc.
        // O mapea manualmente si es más simple (requeriría EAGER loading o findByIdWithDetails)
        // Por simplicidad, recargamos (no es lo más óptimo, pero asegura datos completos):
        Incident detailedIncident = incidentRepository.findAllWithDetails()
                                     .stream().filter(i -> i.getId().equals(updatedIncident.getId()))
                                     .findFirst().orElse(updatedIncident);

        return IncidentDto.fromEntity(detailedIncident);
    }
}