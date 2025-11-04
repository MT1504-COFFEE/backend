package com.ucp.aseo_ucp_backend.repository; 

import java.util.List; // <-- AÑADE ESTE IMPORT
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.User; 

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // --- AÑADE ESTE MÉTODO ---
    // Busca todos los usuarios que coincidan con un rol específico
    List<User> findAllByRole(User.Role role);

    Optional<User> findByResetToken(String resetToken);
}