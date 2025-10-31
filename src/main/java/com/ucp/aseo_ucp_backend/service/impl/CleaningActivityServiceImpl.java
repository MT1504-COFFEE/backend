package com.ucp.aseo_ucp_backend.service.impl; // <- Paquete de implementación

// --- DTOs ---
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.dto.CleaningActivityDto;
import com.ucp.aseo_ucp_backend.dto.CleaningActivityRequest; // <- Importación correcta
import com.ucp.aseo_ucp_backend.entity.Bathroom;
import com.ucp.aseo_ucp_backend.entity.CleaningActivity;
import com.ucp.aseo_ucp_backend.entity.CleaningArea;
import com.ucp.aseo_ucp_backend.entity.Supply;
import com.ucp.aseo_ucp_backend.entity.User; // <- Importa la INTERFAZ
import com.ucp.aseo_ucp_backend.repository.BathroomRepository;
import com.ucp.aseo_ucp_backend.repository.CleaningActivityRepository;
import com.ucp.aseo_ucp_backend.repository.CleaningAreaRepository;
import com.ucp.aseo_ucp_backend.repository.SupplyRepository;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.service.AuthService;
import com.ucp.aseo_ucp_backend.service.CleaningActivityService;


import lombok.RequiredArgsConstructor; // <- Importación de Set

// --- Excepciones (Opcional pero recomendado) ---
// import com.ucp.aseoucpbackend.exception.ResourceNotFoundException;


@Service // <- Anotación @Service
@RequiredArgsConstructor // <- Lombok para inyección de dependencias
public class CleaningActivityServiceImpl implements CleaningActivityService { // <- Implementa la interfaz correcta

    // Campos final para inyección
    private final CleaningActivityRepository activityRepository; // <- Nombre correcto
    private final UserRepository userRepository;
    private final BathroomRepository bathroomRepository;
    private final CleaningAreaRepository cleaningAreaRepository;
    private final SupplyRepository supplyRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Override // <- Anotación @Override correcta
    @Transactional // <- Anotación Transactional
    public CleaningActivity createActivity(CleaningActivityRequest request) { // <- Firma coincide con la interfaz
        User currentUser = authService.getCurrentUser();
        Long bathroomId = request.getBathroomId();
        if (bathroomId == null) {
            // Esta línea no debería alcanzarse si @NotNull está en el DTO y @Valid en el controller
            throw new IllegalArgumentException("El ID del baño (bathroomId) no puede ser nulo.");
        }
        Bathroom bathroom = bathroomRepository.findById(bathroomId) // Usar la variable ya verificada (o no)
                .orElseThrow(() -> new RuntimeException("Baño no encontrado con ID: " + bathroomId));

        CleaningActivity activity = new CleaningActivity();
        activity.setUser(currentUser);
        activity.setBathroom(bathroom);
        activity.setObservations(request.getObservations());

        // Manejar Areas
        if (request.getAreasCleanedIds() != null && !request.getAreasCleanedIds().isEmpty()) {
            Set<CleaningArea> areas = cleaningAreaRepository.findByIdIn(request.getAreasCleanedIds());
            activity.setAreasCleaned(areas);
        } else {
             activity.setAreasCleaned(new HashSet<>());
        }

        // Manejar Supplies
         if (request.getSuppliesRefilledIds() != null && !request.getSuppliesRefilledIds().isEmpty()) {
            Set<Supply> supplies = supplyRepository.findByIdIn(request.getSuppliesRefilledIds());
            activity.setSuppliesRefilled(supplies);
        } else {
            activity.setSuppliesRefilled(new HashSet<>());
        }

        // Convertir fotos a JSON
        try {
            if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
                 activity.setPhotos(objectMapper.writeValueAsString(request.getPhotos()));
            } else {
                activity.setPhotos("[]"); // JSON array vacío
            }
        } catch (JsonProcessingException e) {
            // Loguear el error y lanzar una excepción más específica si es necesario
            throw new RuntimeException("Error al procesar la lista de fotos para JSON", e);
        }

        // Guarda la actividad usando el repositorio inyectado
        return activityRepository.save(activity); // <- Nombre correcto
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleaningActivityDto> getAllActivities() { // Cambia tipo de retorno
    // Necesitas una consulta que cargue las relaciones para evitar N+1 al mapear
    // Crea este método en CleaningActivityRepository
        List<CleaningActivity> activities = activityRepository.findAllWithDetails();

        return activities.stream()
                     .map(CleaningActivityDto::fromEntity) // Mapea a DTO
                     .collect(Collectors.toList());
}

    // ... otros métodos si los defines en la interfaz ...
}