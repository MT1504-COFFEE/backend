package com.ucp.aseo_ucp_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service; // <-- AÑADIR IMPORT
import org.springframework.transaction.annotation.Transactional;

import com.ucp.aseo_ucp_backend.dto.UserDto; // <-- AÑADIR IMPORT
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.exception.ResourceNotFoundException;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.service.AuthService;
import com.ucp.aseo_ucp_backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthService authService; // <-- AÑADIR AUTHSERVICE

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll(); 
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    // --- AÑADIR NUEVO MÉTODO ---
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getId().equals(id)) {
            // Un administrador no puede eliminarse a sí mismo
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        
        User userToDelete = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        userRepository.delete(userToDelete);
    }
    // ---------------------------
}