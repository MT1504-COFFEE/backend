package com.ucp.aseo_ucp_backend.service;

import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
// Ya no importamos 'MessagingException'

public interface EmailService {
    
    // Los métodos ya no declaran 'throws MessagingException'
    void sendNewIncidentNotification(Incident incident);
    
    void sendIncidentAssignmentNotification(Incident incident, User assignedUser);
}