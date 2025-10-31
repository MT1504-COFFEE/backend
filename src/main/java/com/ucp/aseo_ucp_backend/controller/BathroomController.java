package com.ucp.aseo_ucp_backend.controller;

import com.ucp.aseo_ucp_backend.dto.BathroomDto;
import com.ucp.aseo_ucp_backend.service.BathroomService; // Necesitas crear este servicio
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bathrooms")
@RequiredArgsConstructor
public class BathroomController {

    private final BathroomService bathroomService; // Inyecta el servicio

    @GetMapping
    public ResponseEntity<List<BathroomDto>> getBathrooms() {
        List<BathroomDto> bathrooms = bathroomService.getAllBathroomsDto(); // El servicio debe devolver DTOs
        return ResponseEntity.ok(bathrooms);
    }

    // ... otros endpoints para Baños si son necesarios (POST, PUT, DELETE) ...
}