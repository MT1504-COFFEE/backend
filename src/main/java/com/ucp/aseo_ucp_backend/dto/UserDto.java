package com.ucp.aseo_ucp_backend.dto;
import com.ucp.aseo_ucp_backend.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data @NoArgsConstructor @AllArgsConstructor public class UserDto {
    private Long id;
    private String fullName; // Asegúrate que coincida con el nombre en User.java
    private String email;
    private User.Role role; // O String si prefieres devolver el string directamente

    public static UserDto fromEntity(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}