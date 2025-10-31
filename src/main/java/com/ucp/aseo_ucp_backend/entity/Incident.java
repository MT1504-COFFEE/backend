package com.ucp.aseo_ucp_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Table(name = "incidents")
@Data
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('low', 'medium', 'high') DEFAULT 'medium'")
    private Priority priority = Priority.medium; // Valor por defecto

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('pending', 'in_progress', 'resolved') DEFAULT 'pending'")
    private Status status = Status.pending; // Valor por defecto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bathroom_id")
    private Bathroom bathroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Column(columnDefinition = "JSON")
    private String photos; // Almacena array JSON de URLs como String

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime resolvedAt;

    // Enums (pueden ir en archivos separados)
    public enum Priority { low, medium, high }
    public enum Status { pending, in_progress, resolved }

     @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}