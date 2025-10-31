package com.ucp.aseo_ucp_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Excepción para email duplicado
@ResponseStatus(HttpStatus.BAD_REQUEST) // Devuelve 400 Bad Request
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}