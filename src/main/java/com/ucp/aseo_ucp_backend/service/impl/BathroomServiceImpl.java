// Archivo: src/main/java/com/ucp/aseo_ucp_backend/service/impl/BathroomServiceImpl.java
package com.ucp.aseo_ucp_backend.service.impl; // <- Tu paquete de implementación

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service; // <- Importa el repositorio
import org.springframework.transaction.annotation.Transactional; // <- Importa la interfaz del servicio

import com.ucp.aseo_ucp_backend.dto.BathroomDto;
import com.ucp.aseo_ucp_backend.entity.Bathroom; // <- Importa @Service
import com.ucp.aseo_ucp_backend.repository.BathroomRepository;
import com.ucp.aseo_ucp_backend.service.BathroomService;

import lombok.RequiredArgsConstructor;

@Service // <- Anotación @Service
@RequiredArgsConstructor // <- Lombok para inyección
public class BathroomServiceImpl implements BathroomService { // <- Implementa la interfaz

    // El campo final para la inyección
    private final BathroomRepository bathroomRepository; // <- Ahora debería reconocerlo

    @Override // <- Anotación @Override
    @Transactional(readOnly = true) // <- Transacción de solo lectura
    public List<BathroomDto> getAllBathroomsDto() { // <- Firma coincide con la interfaz

        // Llama al método que usa JOIN FETCH del repositorio
        List<Bathroom> bathrooms = bathroomRepository.findAllWithDetails(); // <- Ahora debería reconocerlo

        // El mapeo a DTOs
        return bathrooms.stream()
                        .map(BathroomDto::fromEntity)
                        .collect(Collectors.toList());
    }
}