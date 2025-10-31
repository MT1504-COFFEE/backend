package com.ucp.aseo_ucp_backend.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // Excepción personalizada (crear abajo)
import org.springframework.stereotype.Service;   // Excepción personalizada (crear abajo)
import org.springframework.transaction.annotation.Transactional;

import com.ucp.aseo_ucp_backend.dto.AuthResponse;
import com.ucp.aseo_ucp_backend.dto.LoginRequest;
import com.ucp.aseo_ucp_backend.dto.RegisterRequest;
import com.ucp.aseo_ucp_backend.dto.UserDto;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.exception.EmailAlreadyExistsException;
import com.ucp.aseo_ucp_backend.exception.InvalidRoleException;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.security.JwtUtil;
import com.ucp.aseo_ucp_backend.service.AuthService;

import lombok.RequiredArgsConstructor; // Para register

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional // Recomendado para operaciones que involucran autenticación
    public AuthResponse login(LoginRequest loginRequest) {
        // Autentica usando Spring Security (verifica email y contraseña)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // Si la autenticación es exitosa, la establece en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Busca al usuario para obtener sus detalles completos
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + loginRequest.getEmail()));

        // Genera el token JWT
        String token = jwtUtil.generateToken(user);

        // Devuelve el token y los datos del usuario (DTO)
        return new AuthResponse(token, UserDto.fromEntity(user));
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El email '" + registerRequest.getEmail() + "' ya está registrado.");
        }

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        // Recuerda que tu BD usa 'password' (no 'password_hash' después de la limpieza)
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword())); 
        newUser.setFullName(registerRequest.getFullName());
        try {
            newUser.setRole(User.Role.valueOf(registerRequest.getRole()));
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Rol inválido: " + registerRequest.getRole());
        }

        User savedUser = userRepository.save(newUser);

        // Genera el token JWT para el usuario recién registrado
        String token = jwtUtil.generateToken(savedUser);

        // Devuelve el token y los datos del usuario (DTO)
        return new AuthResponse(token, UserDto.fromEntity(savedUser));
    }

    @Override
    public User getCurrentUser() {
        // Obtiene la autenticación actual del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si hay una autenticación válida
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // No hay usuario autenticado o es anónimo
            return null; // O podrías lanzar una excepción si prefieres
        }

        // El 'name' en la autenticación suele ser el username (nuestro email)
        String userEmail = authentication.getName();

        // Busca al usuario en la base de datos por su email
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario '" + userEmail + "' encontrado en contexto pero no en BD."));
    }
}