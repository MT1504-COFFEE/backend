package com.ucp.aseo_ucp_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate; // <-- Importar RestTemplate
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucp.aseo_ucp_backend.entity.Incident;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.service.EmailService;

import lombok.RequiredArgsConstructor;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    // Ya no usamos JavaMailSender
    // private final JavaMailSender mailSender;
    private final RestTemplate restTemplate; // <-- Usamos RestTemplate
    private final ObjectMapper objectMapper; 
    private final UserRepository userRepository; 

    @Value("${MAILGUN_DOMAIN}")
    private String mailgunDomain;
    
    @Value("${MAILGUN_API_KEY}")
    private String mailgunApiKey; // La clave 'key-...'

    
    @Async
    @Override
    public void sendNewIncidentNotification(Incident incident) {
        try {
            List<User> admins = userRepository.findAllByRole(User.Role.admin);
            if (admins.isEmpty()) {
                System.err.println("ALERTA: No se encontraron administradores para notificar el incidente.");
                return;
            }

            // Convertimos la lista de emails a un string separado por comas
            String adminEmails = admins.stream()
                                       .map(User::getEmail)
                                       .collect(Collectors.joining(","));

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
            
            // Llamamos al método genérico de envío
            sendMailgunMessage(
                "alertas@" + mailgunDomain,
                adminEmails,
                "¡Nuevo Incidente Reportado! - " + incident.getTitle(),
                htmlContent
            );

        } catch (Exception e) {
            System.err.println("Error FATAL al enviar correo de 'nuevo incidente'. El incidente SÍ se guardó.");
            System.err.println("Revisa la configuración de Mailgun (API Key y Dominio).");
            System.err.println("Error de correo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    @Async 
    @Override
    public void sendIncidentAssignmentNotification(Incident incident, User assignedUser) {
        if (assignedUser.getEmail() == null) {
            System.err.println("Error: El usuario " + assignedUser.getFullName() + " no tiene email para notificar.");
            return;
        }

        try {
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
                // ... (resto del HTML se queda igual) ...
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
            
            // Llamamos al método genérico de envío
            sendMailgunMessage(
                "asignaciones@" + mailgunDomain,
                assignedUser.getEmail(),
                "Nueva Tarea Asignada: " + incident.getTitle(),
                htmlContent
            );

        } catch (Exception e) {
            System.err.println("Error FATAL al enviar correo de 'asignación'. El incidente SÍ se asignó.");
            System.err.println("Error de correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // --- NUEVO MÉTODO PRIVADO PARA ENVIAR CON API HTTP ---
    private void sendMailgunMessage(String from, String to, String subject, String html) {
        // 1. Definir la URL de la API de Mailgun
        String apiUrl = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";
        
        // 2. Crear los headers con la Autenticación
        HttpHeaders headers = new HttpHeaders();
        headers.setMediaType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth("api", mailgunApiKey); // Autenticación "Basic" con usuario "api" y la API Key como contraseña

        // 3. Crear el cuerpo (body) del formulario
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("from", from);
        map.add("to", to);
        map.add("subject", subject);
        map.add("html", html);

        // 4. Crear la petición HTTP
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // 5. Enviar la petición
        // El 'try-catch' está en los métodos de arriba, así que esto lanzará el error si falla
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            // Si Mailgun devuelve un error (ej. 401, 400)
            throw new RuntimeException("Fallo al enviar correo: " + response.getBody());
        }
        
        System.out.println("Correo enviado exitosamente a: " + to);
    }

    private String getFirstPhotoUrl(String photosJson) {
        if (photosJson == null || photosJson.isEmpty() || photosJson.equals("[]")) {
            return "";
        }
        try {
            List<String> photoUrls = objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
            if (!photoUrls.isEmpty()) {
                return photoUrls.get(0); 
            }
        } catch (Exception e) {
            System.err.println("Error al parsear JSON de fotos para email: " + e.getMessage());
        }
        return "";
    }
}