package com.ucp.aseo_ucp_backend.service;

import com.ucp.aseo_ucp_backend.dto.AuthResponse;
import com.ucp.aseo_ucp_backend.dto.LoginRequest;
import com.ucp.aseo_ucp_backend.dto.RegisterRequest;
import com.ucp.aseo_ucp_backend.entity.User;

public interface AuthService {
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    User getCurrentUser();

    // --- AÑADIR ESTOS DOS MÉTODOS ---
    // (Esto arreglará los errores de "override")
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}