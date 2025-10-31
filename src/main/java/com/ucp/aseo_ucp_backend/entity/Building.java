package com.ucp.aseo_ucp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
// ... otros imports de lombok si los usas ...

@Entity
@Table(name = "buildings")
@Data // <- Esto crea getName() automáticamente
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address; // Opcional según tu schema

}