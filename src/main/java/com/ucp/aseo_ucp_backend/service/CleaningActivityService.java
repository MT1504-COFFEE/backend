package com.ucp.aseo_ucp_backend.service; // <- Paquete correcto

import java.util.List;

import com.ucp.aseo_ucp_backend.dto.CleaningActivityDto; // <- Importa la entidad
import com.ucp.aseo_ucp_backend.dto.CleaningActivityRequest; // <- Importa List
import com.ucp.aseo_ucp_backend.entity.CleaningActivity;
public interface CleaningActivityService {

    // Firma exacta del método para crear
    CleaningActivity createActivity(CleaningActivityRequest request);

    // Firma exacta del método para obtener todas
    List<CleaningActivityDto> getAllActivities(); // <- Tipo de retorno List<CleaningActivity>
}