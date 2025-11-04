package com.ucp.aseo_ucp_backend.controller; // Verifica tu paquete

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.AuthResponse;
import com.ucp.aseo_ucp_backend.dto.ForgotPasswordRequest;
import com.ucp.aseo_ucp_backend.dto.LoginRequest; // <- ¡Importación añadida!
import com.ucp.aseo_ucp_backend.dto.RegisterRequest;
import com.ucp.aseo_ucp_backend.dto.ResetPasswordRequest;
import com.ucp.aseo_ucp_backend.dto.UserDto; // <-- AÑADIR IMPORT
import com.ucp.aseo_ucp_backend.entity.User; // <-- AÑADIR IMPORT
import com.ucp.aseo_ucp_backend.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Llama al servicio, que ahora devuelve AuthResponse
        AuthResponse authResponse = authService.register(registerRequest);
        
        // Devuelve 201 Created CON el token y el usuario en el body
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        // Ahora Map.of() debería funcionar
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()") // Abierto a todos
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        // La lógica de "no encontrar" se maneja en el servicio
        authService.forgotPassword(request.getEmail());
        // Por seguridad, siempre enviamos una respuesta genérica exitosa
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    @PreAuthorize("permitAll()") // Abierto a todos
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente."));
        } catch (Exception e) {
            // Enviamos el mensaje de error (ej. "Token ha expirado")
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}