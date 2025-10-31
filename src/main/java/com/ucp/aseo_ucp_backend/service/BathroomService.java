package com.ucp.aseo_ucp_backend.service;

import java.util.List;

import com.ucp.aseo_ucp_backend.dto.BathroomDto;

public interface BathroomService {
    List<BathroomDto> getAllBathroomsDto();
    // Puedes añadir más métodos si necesitas (ej. getById, create, update, delete)
}