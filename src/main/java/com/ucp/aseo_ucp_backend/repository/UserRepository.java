package com.ucp.aseo_ucp_backend.repository; // <- Verifica que este sea tu paquete real

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ucp.aseo_ucp_backend.entity.User; // <- Verifica que este sea tu paquete real

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}