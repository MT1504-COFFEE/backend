package com.ucp.aseo_ucp_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Excepción para recursos no encontrados (ej. Baño, Usuario)
@ResponseStatus(HttpStatus.NOT_FOUND) // Devuelve 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(String.format("%s no encontrado con %s : '%s'", resourceType, field, value));
    }
     public ResourceNotFoundException(String message) {
        super(message);
    }
}