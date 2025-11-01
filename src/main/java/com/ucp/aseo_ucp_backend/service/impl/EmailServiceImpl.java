package com.ucp.aseo_ucp_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper; // Para parsear las fotos

    @Value("${admin.notification.email}")
    private String adminEmail; // El email del admin (desde application.properties)

    @Value("${spring.mail.username}")
    private String fromEmail; // El email que envía

    // Habilita @Async en tu AseoUcpBackendApplication.java
    // añadiendo @EnableAsync en la clase
    // @Async
    @Override
    public void sendNewIncidentNotification(Incident incident) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(adminEmail);
        helper.setSubject("¡Nuevo Incidente Reportado! - " + incident.getTitle());

        String bathroomName = incident.getBathroom().getName();
        String buildingName = incident.getBathroom().getBuilding() != null ? 
                              incident.getBathroom().getBuilding().getName() : "N/A";
        String reportedBy = incident.getReportedBy().getFullName();

        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Se ha reportado un nuevo incidente:</h2>" +
            "<p><strong>Título:</strong> %s</p>" +
            "<p><strong>Prioridad:</strong> %s</p>" +
            "<p><strong>Reportado por:</strong> %s</p>" +
            "<p><strong>Edificio:</strong> %s</p>" +
            "<p><strong>Baño:</strong> %s</p>" +
            "<p><strong>Descripción:</strong> %s</p>" +
            "<p>Por favor, ingrese al panel de administración para asignarlo.</p>" +
            "</body></html>",
            incident.getTitle(),
            incident.getPriority().name(),
            reportedBy,
            buildingName,
            bathroomName,
            incident.getDescription()
        );
        
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    // @Async
    @Override
    public void sendIncidentAssignmentNotification(Incident incident, User assignedUser) throws MessagingException {
        if (assignedUser.getEmail() == null) {
            System.err.println("Error: El usuario " + assignedUser.getFullName() + " no tiene email para notificar.");
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(assignedUser.getEmail()); // Se envía al colaborador
        helper.setSubject("Nueva Tarea Asignada: " + incident.getTitle());

        // Extraer detalles
        String bathroomName = incident.getBathroom().getName();
        String buildingName = incident.getBathroom().getBuilding() != null ? 
                              incident.getBathroom().getBuilding().getName() : "N/A";
        String floor = incident.getBathroom().getFloor() != null ? 
                       String.valueOf(incident.getBathroom().getFloor().getFloorNumber()) : "N/A";
        String time = incident.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));
        String firstPhotoUrl = getFirstPhotoUrl(incident.getPhotos());


        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Hola %s, se te ha asignado un nuevo incidente:</h2>" +
            "<p><strong>Título:</strong> %s</p>" +
            "<p><strong>Prioridad:</strong> %s</p>" +
            "<p><strong>Reportado el:</strong> %s</p>" +
            "<hr>" +
            "<h3>Detalles de Ubicación:</h3>" +
            "<p><strong>Edificio:</strong> %s</p>" +
            "<p><strong>Piso:</strong> %s</p>" +
            "<p><strong>Baño:</strong> %s</p>" +
            "<hr>" +
            "<h3>Descripción del Problema:</h3>" +
            "<p>%s</p>" +
            "%s" + // Bloque de imagen
            "<p>Por favor, atiende este incidente y márcalo como resuelto en la aplicación cuando termines.</p>" +
            "</body></html>",
            assignedUser.getFullName(),
            incident.getTitle(),
            incident.getPriority().name(),
            time,
            buildingName,
            floor,
            bathroomName,
            incident.getDescription(),
            firstPhotoUrl.isEmpty() ? "<p><em>No se adjuntó imagen.</em></p>" : 
            "<p><strong>Evidencia:</strong><br/><img src='" + firstPhotoUrl + "' alt='Evidencia del incidente' style='max-width: 400px; height: auto;' /></p>"
        );

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    // Método privado para obtener la primera foto de la lista JSON
    private String getFirstPhotoUrl(String photosJson) {
        if (photosJson == null || photosJson.isEmpty() || photosJson.equals("[]")) {
            return "";
        }
        try {
            List<String> photoUrls = objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
            if (!photoUrls.isEmpty()) {
                return photoUrls.get(0); // Devuelve la primera URL
            }
        } catch (Exception e) {
            System.err.println("Error al parsear JSON de fotos para email: " + e.getMessage());
        }
        return "";
    }

    
}