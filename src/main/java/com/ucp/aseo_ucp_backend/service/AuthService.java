package com.ucp.aseo_ucp_backend.service;

import com.ucp.aseo_ucp_backend.dto.AuthResponse;
import com.ucp.aseo_ucp_backend.dto.LoginRequest;
import com.ucp.aseo_ucp_backend.dto.RegisterRequest;
import com.ucp.aseo_ucp_backend.entity.User; // Importa tu entidad User

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    User getCurrentUser(); // Devuelve la entidad User completa
}