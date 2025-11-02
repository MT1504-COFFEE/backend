package com.ucp.aseo_ucp_backend.service.impl; 

import java.time.LocalDateTime; 
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.dto.IncidentDto;
import com.ucp.aseo_ucp_backend.dto.IncidentRequest;
import com.ucp.aseo_ucp_backend.dto.UpdateIncidentStatusRequest; 
import com.ucp.aseo_ucp_backend.entity.Bathroom;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.exception.ResourceNotFoundException; 
import com.ucp.aseo_ucp_backend.repository.BathroomRepository;
import com.ucp.aseo_ucp_backend.repository.IncidentRepository;
import com.ucp.aseo_ucp_backend.repository.UserRepository; 
import com.ucp.aseo_ucp_backend.service.AuthService;
import com.ucp.aseo_ucp_backend.service.EmailService;
import com.ucp.aseo_ucp_backend.service.IncidentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final BathroomRepository bathroomRepository;
    private final UserRepository userRepository; 
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService; 

    @Override
    @Transactional
    public Incident createIncident(IncidentRequest request) {
         User currentUser = authService.getCurrentUser();
         if (currentUser == null) {
             throw new RuntimeException("Usuario no autenticado para reportar incidente.");
         }

         Bathroom bathroom = bathroomRepository.findById(request.getBathroomId())
                 .orElseThrow(() -> new ResourceNotFoundException("Baño no encontrado con ID: " + request.getBathroomId()));

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

         Incident savedIncident = incidentRepository.save(incident); 

         Incident detailedIncident = incidentRepository.findByIdWithDetails(savedIncident.getId())
                                       .orElse(savedIncident); 
         
         // --- LLAMADA SIMPLIFICADA ---
         // Ya no necesitamos try-catch. Si el correo falla,
         // el @Async y el try-catch en EmailServiceImpl se encargarán.
         emailService.sendNewIncidentNotification(detailedIncident);
         // ------------------------------------

         return savedIncident; 
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentDto> getAllIncidents() { 
        List<Incident> incidents = incidentRepository.findAllWithDetails();

        return incidents.stream()
                        .map(IncidentDto::fromEntity) 
                        .collect(Collectors.toList()); 
    }

    @Override
    @Transactional
    public IncidentDto updateIncidentStatus(Long id, UpdateIncidentStatusRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));
        
        Incident.Status newStatus = request.getStatus();
        incident.setStatus(newStatus);
        
        User userToNotify = null; 

        if (newStatus == Incident.Status.in_progress) {
            if (request.getAssignedUserId() == null) {
                throw new IllegalArgumentException("Se requiere un ID de usuario para asignar el incidente.");
            }
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getAssignedUserId()));
            
            if (assignedUser.getRole() != User.Role.cleaning_staff) {
                 throw new IllegalArgumentException("Solo se puede asignar incidentes al personal de limpieza.");
            }

            incident.setAssignedTo(assignedUser);
            incident.setResolvedAt(null); 
            
            userToNotify = assignedUser; 

        } else if (newStatus == Incident.Status.resolved) {
            incident.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == Incident.Status.pending) {
            incident.setAssignedTo(null);
            incident.setResolvedAt(null);
        }

        incidentRepository.save(incident); 
        
        Incident detailedIncident = incidentRepository.findByIdWithDetails(id)
                                     .orElseThrow(() -> new ResourceNotFoundException("Incidente", "id", id));

        // --- LLAMADA SIMPLIFICADA ---
        if (userToNotify != null) {
             // Ya no necesitamos try-catch.
            emailService.sendIncidentAssignmentNotification(detailedIncident, userToNotify);
        }
        // ------------------------------------------------

        return IncidentDto.fromEntity(detailedIncident);
    }
}