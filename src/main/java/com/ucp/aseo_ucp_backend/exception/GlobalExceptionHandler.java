package com.ucp.aseo_ucp_backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice // Indica que esta clase manejará excepciones globalmente
public class GlobalExceptionHandler {

    // Maneja excepciones de validación (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Maneja email duplicado
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExists(EmailAlreadyExistsException ex, WebRequest request) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

     // Maneja rol inválido
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<Object> handleInvalidRole(InvalidRoleException ex, WebRequest request) {
        Map<String, String> body = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    // Maneja usuario no encontrado (login o carga de UserDetails)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
         Map<String, String> body = Map.of("error", "Credenciales inválidas."); // Mensaje genérico por seguridad
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED); // 401
    }

     // Maneja otras excepciones de autenticación/autorización de Spring Security
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        Map<String, String> body = Map.of("error", "Acceso denegado o credenciales inválidas.");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED); // 401
    }


    // Maneja recursos no encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
         Map<String, String> body = Map.of("error", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND); // 404
    }

    // Maneja errores de almacenamiento de archivos
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<Object> handleFileStorageException(FileStorageException ex, WebRequest request) {
         Map<String, String> body = Map.of("error", "Error al procesar el archivo: " + ex.getMessage());
         // Podrías devolver 400 si es un problema del archivo, o 500 si es del servidor
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // Manejador genérico para otras RuntimeException no capturadas específicamente
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleGenericRuntimeException(RuntimeException ex, WebRequest request) {
        // Loguea el error completo para depuración interna
        ex.printStackTrace(); // ¡Usa un logger real en producción!
        Map<String, String> body = Map.of("error", "Ocurrió un error inesperado en el servidor.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR); // 500
    }

     // Manejador genérico para cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        ex.printStackTrace(); // ¡Usa un logger real!
        Map<String, String> body = Map.of("error", "Ocurrió un error interno.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}