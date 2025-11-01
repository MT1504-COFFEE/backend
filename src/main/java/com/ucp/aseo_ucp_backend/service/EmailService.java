package com.ucp.aseo_ucp_backend.service;

import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import jakarta.mail.MessagingException;

public interface EmailService {
    
    // Correo para el Admin cuando se crea un incidente
    void sendNewIncidentNotification(Incident incident) throws MessagingException;
    
    // Correo para el Colaborador cuando se le asigna
    void sendIncidentAssignmentNotification(Incident incident, User assignedUser) throws MessagingException;
}