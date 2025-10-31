package com.ucp.aseo_ucp_backend.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Table(name = "cleaning_activities")
@Data
public class CleaningActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bathroom_id")
    private Bathroom bathroom;

    @Lob
    private String observations;

    // Mapeo para ManyToMany con CleaningArea
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "activity_areas_cleaned",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "area_id")
    )
    private Set<CleaningArea> areasCleaned = new HashSet<>();

    // Mapeo para ManyToMany con Supply
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "activity_supplies_refilled",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "supply_id")
    )
    private Set<Supply> suppliesRefilled = new HashSet<>();


    @Column(columnDefinition = "JSON") // O usa String si prefieres manejar la conversión manualmente
    private String photos; // Almacena un array JSON de URLs como String

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

     @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}