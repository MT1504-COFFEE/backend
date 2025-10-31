package com.ucp.aseo_ucp_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Excepción genérica para problemas de almacenamiento de archivos
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Devuelve 500
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}