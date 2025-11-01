package com.ucp.aseo_ucp_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ucp.aseo_ucp_backend.dto.UserDto;
import com.ucp.aseo_ucp_backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin')") // Solo los admins pueden ver la lista
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            // Devuelve una respuesta simple de éxito
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            // Error si intenta borrarse a sí mismo
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Otro error (ej. usuario no encontrado)
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    // ----------------------------
}