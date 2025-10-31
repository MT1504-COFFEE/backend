package com.ucp.aseo_ucp_backend.controller; // Verifica tu paquete

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.AuthResponse;
import com.ucp.aseo_ucp_backend.dto.LoginRequest;
import com.ucp.aseo_ucp_backend.dto.RegisterRequest;
import com.ucp.aseo_ucp_backend.dto.UserDto; // <- ¡Importación añadida!
import com.ucp.aseo_ucp_backend.entity.User;
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
}