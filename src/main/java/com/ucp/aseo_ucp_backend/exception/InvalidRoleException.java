package com.ucp.aseo_ucp_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Excepción para rol inválido
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException(String message) {
        super(message);
    }
}