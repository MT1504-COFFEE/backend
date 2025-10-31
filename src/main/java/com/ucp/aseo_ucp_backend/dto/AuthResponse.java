package com.ucp.aseo_ucp_backend.dto;
import lombok.*;
@Data @AllArgsConstructor public class AuthResponse {
    private String token;
    private UserDto user;
}