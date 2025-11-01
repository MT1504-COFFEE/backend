package com.ucp.aseo_ucp_backend.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;   
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

import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional 
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + loginRequest.getEmail()));

        String token = jwtUtil.generateToken(user);
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
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword())); 
        newUser.setFullName(registerRequest.getFullName());
        
        // --- ¡CAMBIO IMPORTANTE DE SEGURIDAD! ---
        // Ignoramos el rol que viene del request y forzamos a "cleaning_staff"
        newUser.setRole(User.Role.cleaning_staff);
        // ---------------------------------------

        User savedUser = userRepository.save(newUser);
        String token = jwtUtil.generateToken(savedUser);
        return new AuthResponse(token, UserDto.fromEntity(savedUser));
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null; 
        }

        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario '" + userEmail + "' encontrado en contexto pero no en BD."));
    }
}