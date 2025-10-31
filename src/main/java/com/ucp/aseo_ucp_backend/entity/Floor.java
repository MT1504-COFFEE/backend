package com.ucp.aseo_ucp_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "floors")
@Data
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Evita cargar Building innecesariamente
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    // Podrías añadir un campo 'name' si lo necesitas (ej. "Planta Baja")
    // private String name;
}